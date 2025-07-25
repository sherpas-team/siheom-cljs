// https://vitejs.dev/config/
export default {
    define: {
        'process.env': process.env
    },
    test: {
        globals: true,
        environment: 'jsdom',
        optimizer: {
            web: {
                include: ['vitest-canvas-mock']
            }
        },
        browser: {
            enabled: true,
            headless: true,
            provider: "playwright",
            fileParallelism: true,
            instances: [
                {
                    browser: 'chromium',
                    context: {
                        timezoneId: 'Asia/Seoul',
                        permissions: ["clipboard-read"]
                    },
                },
            ],
        },
        exclude: ['node_modules', 'dist', '.idea', '.git', '.cache', 'e2e'],
        include: ['./target/vitest/js/*-test.js'],
        setupFiles: ['./test/setupTest.js'],
        css: true,
        pool: 'vmThreads',
        poolOptions: {
            useAtomics: true
        },
        environmentOptions: {
            jsdom: {
                resources: 'usable',
            },
        },
        onConsoleLog(log, type) {
        },
    }
}
