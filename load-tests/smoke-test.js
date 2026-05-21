import { check } from 'k6';
import http from 'k6/http';
import { BASE_URL } from './common.js';

const DEVICE_ID = 'k6-smoke-device';
const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/;

function params(name, extraHeaders = {}) {
    return {
        headers: {
            'X-Device-Id': DEVICE_ID,
            ...extraHeaders,
        },
        tags: { name },
    };
}

export const options = {
    vus: 1,
    iterations: 1,
    thresholds: { http_req_failed: ['rate==0'] },
};

export default function () {
    const userRes = http.get(`${BASE_URL}/api/createUserId`, params('GET /api/createUserId'));
    check(userRes, {
        'createUserId 200': (r) => r.status === 200,
        'createUserId UUID': (r) => r.body != null && UUID_PATTERN.test(r.body.replace(/"/g, '').trim()),
    });
    if (userRes.status !== 200 || userRes.body == null) return;
    const userId = userRes.body.replace(/"/g, '').trim();

    check(http.get(`${BASE_URL}/api/plp`, params('GET /api/plp')), { 'plp 200': (r) => r.status === 200 });
    check(http.get(`${BASE_URL}/api/pdp/1000?osType=ANDROID`, params('GET /api/pdp/{sku}')), { 'pdp 200': (r) => r.status === 200 });
    const addRes = http.post(`${BASE_URL}/api/basket`,
        JSON.stringify({ userId, sku: 1000, quantity: 1 }),
        params('POST /api/basket', { 'Content-Type': 'application/json' }));
    check(addRes, { 'basket add 200': (r) => r.status === 200 });
    check(http.get(`${BASE_URL}/api/basket?userId=${userId}`, params('GET /api/basket')),
        { 'basket get 200': (r) => r.status === 200 });
}
