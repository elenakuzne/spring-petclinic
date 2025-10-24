package org.springframework.samples.petclinic.selenium;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.test.LocalServerPort;

import java.time.Duration;

/**
 * Base class for Selenium E2E tests that provides WebDriver setup and teardown.
 * Browser instance is reused across all tests for better performance.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseSeleniumTest {

	protected static WebDriver driver;

	@LocalServerPort
	protected int port;

	protected String baseUrl;

	@BeforeAll
	public static void setUpBrowser() {
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless");
		options.addArguments("--no-sandbox");
		options.addArguments("--disable-dev-shm-usage");
		options.addArguments("--disable-gpu");
		options.addArguments("--window-size=1920,1080");

		driver = new ChromeDriver(options);
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
	}

	@BeforeEach
	public void setUp() {
		baseUrl = "http://localhost:" + port;
		// Clean state between tests - navigate to home and clear cookies
		driver.get(baseUrl);
		driver.manage().deleteAllCookies();
	}

	@AfterEach
	public void cleanUpAfterTest() {
		// Optional: reset database state
	}

	@AfterAll
	public static void tearDownBrowser() {
		if (driver != null) {
			driver.quit();
		}
	}
}
