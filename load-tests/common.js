import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

export const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

const SKUS = [1000, 1001, 1002, 1003, 1004, 1005, 1006, 1007];

export const createUserTrend = new Trend('lat_create_user');
export const plpTrend = new Trend('lat_plp');
export const pdpTrend = new Trend('lat_pdp');
export const addBasketTrend = new Trend('lat_basket_add');
export const getBasketTrend = new Trend('lat_basket_get');
export const errorRate = new Rate('errors');
export const flowsCompleted = new Counter('flows_completed');

export function randomIntBetween(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

function buildDeviceId() {
    const prefix = __ENV.DEVICE_ID_PREFIX || 'k6-device';
    return `${prefix}-${__VU}-${__ITER}-${randomIntBetween(100000, 999999)}`;
}

function params(name, deviceId, extraHeaders = {}) {
    return {
        headers: {
            'X-Device-Id': deviceId,
            ...extraHeaders,
        },
        tags: { name },
    };
}

function check200(res, name, trend) {
    const ok = check(res, {
        [`${name}: status 200`]: (r) => r.status === 200,
    });
    trend.add(res.timings.duration);
    errorRate.add(ok ? 0 : 1);
    return ok;
}

function parseUserId(res) {
    return res.body.replace(/"/g, '').trim();
}

export function runBuyerFlow({ thinkTime = true, osType = 'ANDROID' } = {}) {
    const deviceId = buildDeviceId();
    let userId;

    group('1. createUserId', () => {
        const res = http.get(`${BASE_URL}/api/createUserId`, params('GET /api/createUserId', deviceId));
        if (check200(res, 'createUserId', createUserTrend)) {
            userId = parseUserId(res);
            check(userId, {
                'createUserId: valid UUID': (id) =>
                    /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/.test(id),
            });
        }
    });
    if (!userId) return;
    if (thinkTime) sleep(randomIntBetween(1, 3) / 10);

    group('2. PLP', () => {
        const res = http.get(`${BASE_URL}/api/plp`, params('GET /api/plp', deviceId));
        check200(res, 'plp', plpTrend);
    });
    if (thinkTime) sleep(randomIntBetween(2, 6) / 10);

    const sku = SKUS[randomIntBetween(0, SKUS.length - 1)];
    group('3. PDP', () => {
        const res = http.get(`${BASE_URL}/api/pdp/${sku}?osType=${osType}`, params('GET /api/pdp/{sku}', deviceId));
        check200(res, 'pdp', pdpTrend);
    });
    if (thinkTime) sleep(randomIntBetween(3, 8) / 10);

    group('4. add to basket', () => {
        const payload = JSON.stringify({
            userId,
            sku,
            quantity: randomIntBetween(1, 3),
        });
        const res = http.post(
            `${BASE_URL}/api/basket`,
            payload,
            params('POST /api/basket', deviceId, { 'Content-Type': 'application/json' }),
        );
        check200(res, 'basket_add', addBasketTrend);
    });
    if (thinkTime) sleep(randomIntBetween(1, 3) / 10);

    group('5. get basket', () => {
        const res = http.get(`${BASE_URL}/api/basket?userId=${userId}`, params('GET /api/basket', deviceId));
        check200(res, 'basket_get', getBasketTrend);
    });

    flowsCompleted.add(1);
    if (thinkTime) sleep(randomIntBetween(1, 4) / 10);
}
