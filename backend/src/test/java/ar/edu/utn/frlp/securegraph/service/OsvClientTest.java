package ar.edu.utn.frlp.securegraph.service;

import ar.edu.utn.frlp.securegraph.service.OsvClient.MavenPackage;
import ar.edu.utn.frlp.securegraph.service.OsvClient.OsvException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.net.SocketTimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class OsvClientTest {

    private static final String ENDPOINT = "https://api.osv.dev/v1/query";
    private static final MavenPackage MAVEN = new MavenPackage("example:library", "1.0");
    private final RestClient.Builder builder = RestClient.builder();
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    private final OsvClient client = new OsvClient(builder.build(), "https://api.osv.dev/v1");

    @Test
    void readsAllPagesAndPreservesIdentifiersAndOptionalCvss() {
        expect("""
                {"package":{"ecosystem":"Maven","name":"example:library"},"version":"1.0"}
                """).andRespond(withSuccess("""
                {"vulns":[{"id":"GHSA-aaaa-bbbb-cccc","aliases":["CVE-2021-12345"],
                  "severity":[{"type":"CVSS_V3","score":"CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:C/C:H/I:H/A:H"}]}],
                 "next_page_token":"next"}
                """, MediaType.APPLICATION_JSON));
        expect("""
                {"package":{"ecosystem":"Maven","name":"example:library"},"version":"1.0","page_token":"next"}
                """).andRespond(withSuccess("""
                {"vulns":[{"id":"OSV-2021-1","database_specific":{"severity":"MODERATE"}}]}
                """, MediaType.APPLICATION_JSON));

        var result = client.query(MAVEN);
        assertThat(result).satisfiesExactly(first -> {
            assertThat(first.identifiers()).containsExactly("CVE-2021-12345", "GHSA-aaaa-bbbb-cccc");
            assertThat(first.cvss()).isEqualByComparingTo("10.0");
            assertThat(first.severity()).isEqualTo("CRITICAL");
        }, second -> {
            assertThat(second.cvss()).isNull();
            assertThat(second.severity()).isEqualTo("MEDIUM");
        });
        server.verify();
    }

    @Test
    void treatsAnEmptyObjectAsSuccessfulWithoutFindings() {
        server.expect(requestTo(ENDPOINT)).andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        assertThat(client.query(MAVEN)).isEmpty();
        server.verify();
    }

    @Test
    void treatsInvalidResponsesAndBadGatewayAsFailures() {
        server.expect(requestTo(ENDPOINT)).andRespond(withSuccess("{\"vulns\":{}}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(ENDPOINT)).andRespond(withStatus(HttpStatus.BAD_GATEWAY));
        for (int attempt = 0; attempt < 2; attempt++) {
            var failure = catchThrowableOfType(() -> client.query(MAVEN), OsvException.class);
            assertThat(failure).isNotNull();
            assertThat(failure.isUnavailable()).isFalse();
        }
        server.verify();
    }

    @Test
    void treatsRateLimitsAndTimeoutsAsUnavailable() {
        server.expect(requestTo(ENDPOINT)).andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));
        server.expect(requestTo(ENDPOINT)).andRespond(withException(new SocketTimeoutException("timed out")));
        for (int attempt = 0; attempt < 2; attempt++) {
            var failure = catchThrowableOfType(() -> client.query(MAVEN), OsvException.class);
            assertThat(failure).isNotNull();
            assertThat(failure.isUnavailable()).isTrue();
        }
        server.verify();
    }

    @Test
    void computesCvssBaseValuesAndLeavesUnsupportedOrIncompleteValuesAbsent() {
        assertThat(CvssScore.parse("CVSS_V3", "CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:H/A:H"))
                .isEqualByComparingTo("9.8");
        assertThat(CvssScore.parse("CVSS_V3", "CVSS:3.0/AV:N/AC:L/PR:N/UI:N/S:U/C:N/I:N/A:N"))
                .isEqualByComparingTo("0");
        assertThat(CvssScore.parse("CVSS_V3", "CVSS:3.1/AV:N")).isNull();
        assertThat(CvssScore.parse("CVSS_V2", "AV:N/AC:L/Au:N/C:C/I:C/A:C")).isEqualByComparingTo("10.0");
        assertThat(CvssScore.parse("CVSS_V4",
                "CVSS:4.0/AV:N/AC:L/AT:N/PR:N/UI:N/VC:H/VI:H/VA:H/SC:H/SI:H/SA:H"))
                .isEqualByComparingTo("10.0");
        assertThat(CvssScore.parse("CVSS_V4", "10.1")).isNull();
    }

    private org.springframework.test.web.client.ResponseActions expect(String payload) {
        return server.expect(requestTo(ENDPOINT)).andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(content().json(payload, true));
    }
}
