function fn() {
    var config = {
        baseUrl: 'http://localhost:8080',
        mockServerUrl: 'http://localhost:8080/mock'
    };
 config.adminToken = 'Bearer admin-token';
  config.userNoPermissionToken = 'Bearer user-no-permission';
    // Configuration spécifique par environnement
    if (karate.env === 'dev') {
        config.baseUrl = 'http://dev.example.com';
    } else if (karate.env === 'qa') {
        config.baseUrl = 'http://qa.example.com';
    }

    return config;
}