package ci;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CiSmokeTest {

    @Test
    void ci_smoke() {
        System.out.println("CI smoke test is running.");
        assertTrue(true);
    }
}
