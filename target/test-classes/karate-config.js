function fn() {
    var config = {
        baseUrl: 'http://localhost:8080',
        mockServerUrl: 'http://localhost:8080/mock'
    };

    // Configuration spécifique par environnement
    if (karate.env === 'dev') {
        config.baseUrl = 'http://dev.example.com';
    } else if (karate.env === 'qa') {
        config.baseUrl = 'http://qa.example.com';
    }

    return config;
}