package co.com.dgallego58.access;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(path = "/session")
public class AccessController {

    private final HttpSession instanceHttpSession;
    private final HttpServletRequest httpServletRequest;
    @Autowired
    UserDetailsService userDetailsService;

    public AccessController(HttpSession httpSession, HttpServletRequest httpServletRequest) {
        this.instanceHttpSession = httpSession;
        this.httpServletRequest = httpServletRequest;
    }

    @GetMapping(path = "/put")
    public ResponseEntity<Void> session(HttpSession httpSession) {
        httpSession.setAttribute("tkn", "val");
        httpSession.setAttribute("queso", "mantequilla");
        return ResponseEntity.noContent().build();
    }

    @GetMapping(path = "/get")
    public ResponseEntity<JsonNode> getVal() {
        var obj = JsonMapper.builder().build().createObjectNode()
                            .put("tkn", (String) instanceHttpSession.getAttribute("tkn"))
                            .put("test", "works!");
        return ResponseEntity.ok(obj);
    }

    @GetMapping(path = "/cookies/{cookie}")
    public ResponseEntity<Map<String, String>> cookie(@PathVariable String cookie, HttpSession httpSession) {
        var cookies = new HashMap<String, String>();
        cookies.put("sessionId", instanceHttpSession.getId());
        cookies.put("tkn", "token");
        cookies.put(cookie, "da cookie");
        cookies.put("set-cookie", httpServletRequest.getHeader("Set-Cookie"));
        cookies.forEach(instanceHttpSession::setAttribute);
        return ResponseEntity.ok(cookies);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<Map<String, String>> getId(@PathVariable String id) {
        return ResponseEntity.ok(db().get(id));
    }

    @PostMapping(path = "/authenticate")
    public ResponseEntity<JsonNode> login(@RequestBody JsonNode jsonNode) {
        var user = db().get(jsonNode.get("id").asText());
        var expirationAt = Date.from(Instant.now().plus(Duration.ofMinutes(3)));
        var jwt = JWT.create()
                     .withSubject(user.get("username"))
                     .withExpiresAt(expirationAt)
                     .sign(Algorithm.HMAC512(System.getProperty("jwt-secret")));
        var response = JsonMapper.builder()
                                 .build()
                                 .createObjectNode()
                                 .put("token", jwt)
                                 .put("type", "bearer");

        return ResponseEntity.ok(response);
    }

    private Map<String, Map<String, String>> db() {
        var map = new HashMap<String, Map<String, String>>();
        map.put("1", new HashMap<>() {{
            put("username", "test1");
        }});
        map.put("2", new HashMap<>() {{
            put("username", "test2");
        }});
        return map;
    }
}
