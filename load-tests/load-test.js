import { runBuyerFlow } from './common.js';

export const options = {
    scenarios: {
        ramp_load: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '30s', target: 20 },
                { duration: '1m',  target: 50 },
                { duration: '1m',  target: 100 },
                { duration: '1m',  target: 100 },
                { duration: '30s', target: 0 },
            ],
            gracefulRampDown: '10s',
        },
    },
    thresholds: {
        http_req_failed:   ['rate<0.01'],
        http_req_duration: ['p(95)<500', 'p(99)<1000'],
        errors:            ['rate<0.01'],
        lat_plp:           ['p(95)<300'],
        lat_pdp:           ['p(95)<300'],
        lat_basket_add:    ['p(95)<400'],
    },
};

export default function () {
    runBuyerFlow();
}
