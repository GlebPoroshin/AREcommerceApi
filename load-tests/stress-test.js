import { runBuyerFlow } from './common.js';

export const options = {
    scenarios: {
        stress: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '30s', target: 200 },
                { duration: '1m',  target: 400 },
                { duration: '1m',  target: 600 },
                { duration: '30s', target: 0 },
            ],
            gracefulRampDown: '10s',
        },
    },
    thresholds: {
        http_req_failed:   ['rate<0.05'],
        http_req_duration: ['p(95)<2000'],
        errors:            ['rate<0.05'],
    },
};

export default function () {
    runBuyerFlow({ thinkTime: false });
}
