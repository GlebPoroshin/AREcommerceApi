import { runBuyerFlow } from './common.js';

const STEP_DURATION = __ENV.STEP_DURATION || '30s';

export const options = {
    scenarios: {
        vus_100: {
            executor: 'constant-vus',
            vus: 100,
            duration: STEP_DURATION,
            startTime: '0s',
            gracefulStop: '10s',
        },
        vus_200: {
            executor: 'constant-vus',
            vus: 200,
            duration: STEP_DURATION,
            startTime: '40s',
            gracefulStop: '10s',
        },
        vus_300: {
            executor: 'constant-vus',
            vus: 300,
            duration: STEP_DURATION,
            startTime: '1m20s',
            gracefulStop: '10s',
        },
        vus_400: {
            executor: 'constant-vus',
            vus: 400,
            duration: STEP_DURATION,
            startTime: '2m0s',
            gracefulStop: '10s',
        },
        vus_600: {
            executor: 'constant-vus',
            vus: 600,
            duration: STEP_DURATION,
            startTime: '2m40s',
            gracefulStop: '10s',
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.05'],
        errors: ['rate<0.05'],
        'http_req_duration{scenario:vus_100}': ['p(95)<10000'],
        'http_req_duration{scenario:vus_200}': ['p(95)<10000'],
        'http_req_duration{scenario:vus_300}': ['p(95)<10000'],
        'http_req_duration{scenario:vus_400}': ['p(95)<10000'],
        'http_req_duration{scenario:vus_600}': ['p(95)<10000'],
    },
};

export default function () {
    runBuyerFlow({ thinkTime: false });
}
