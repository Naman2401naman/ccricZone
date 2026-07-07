package com.criczone.demo.security;

import javax.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthRateLimitFilterTest {

    @Test
    void loginLimitReturnsTooManyRequests() throws Exception {
        AuthRateLimitFilter filter = new AuthRateLimitFilter();
        FilterChain chain = (request, response) -> {
        };

        for (int i = 0; i < 10; i++) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(loginRequest(), response, chain);
            assertEquals(HttpStatus.OK.value(), response.getStatus());
        }

        MockHttpServletResponse limitedResponse = new MockHttpServletResponse();
        filter.doFilter(loginRequest(), limitedResponse, chain);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), limitedResponse.getStatus());
    }

    private MockHttpServletRequest loginRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/users/login");
        request.setRemoteAddr("10.0.0.10");
        return request;
    }
}
