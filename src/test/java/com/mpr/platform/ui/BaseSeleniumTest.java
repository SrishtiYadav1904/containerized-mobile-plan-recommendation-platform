package com.mpr.platform.ui;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Base Selenium test harness providing driver lifecycle, explicit wait utilities,
 * configurable base URL, and automated screenshot capture on test failure.
 */
public abstract class BaseSeleniumTest {

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected String baseUrl;

    private static final String DEFAULT_BASE_URL = "http://localhost:8084/mpr-platform";
    private static final Path SCREENSHOTS_DIR = Paths.get("target", "screenshots");

    @BeforeAll
    public static void setupWebDriverManager() {
        // Automatically manages ChromeDriver compatible with local Chrome installation
        try {
            WebDriverManager.chromedriver().setup();
        } catch (Exception e) {
            System.out.println("[WARN] WebDriverManager setup warning: " + e.getMessage() + ". Relying on Selenium Manager fallback.");
        }
    }

    @BeforeEach
    public void setUp() {
        // Resolve base URL from system property or environment variable
        String propUrl = System.getProperty("test.base.url");
        String envUrl = System.getenv("APP_BASE_URL");
        if (propUrl != null && !propUrl.trim().isEmpty()) {
            baseUrl = propUrl.trim();
        } else if (envUrl != null && !envUrl.trim().isEmpty()) {
            baseUrl = envUrl.trim();
        } else {
            baseUrl = DEFAULT_BASE_URL;
        }

        // Configure Chrome options
        ChromeOptions options = new ChromeOptions();
        boolean headless = Boolean.parseBoolean(System.getProperty("headless", "true"));
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception ignored) {
            }
        }
    }

    @RegisterExtension
    public AfterTestExecutionCallback screenshotOnFailureWatcher = context -> {
        if (context.getExecutionException().isPresent()) {
            captureFailureDiagnostics(context.getRequiredTestClass().getSimpleName() + "_" + context.getRequiredTestMethod().getName());
        }
    };

    /**
     * Captures browser screenshot and DOM page source for failure diagnostics.
     */
    protected void captureFailureDiagnostics(String testIdentifier) {
        if (driver == null) {
            return;
        }
        try {
            Files.createDirectories(SCREENSHOTS_DIR);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            // 1. Capture PNG Screenshot
            if (driver instanceof TakesScreenshot) {
                File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                Path destFile = SCREENSHOTS_DIR.resolve(testIdentifier + "_" + timestamp + ".png");
                Files.copy(scrFile.toPath(), destFile);
                System.out.println("[DIAGNOSTIC] Screenshot saved: " + destFile.toAbsolutePath());
            }

            // 2. Dump HTML Page Source
            String pageSource = driver.getPageSource();
            Path sourceFile = SCREENSHOTS_DIR.resolve(testIdentifier + "_" + timestamp + ".html");
            Files.writeString(sourceFile, pageSource);
            System.out.println("[DIAGNOSTIC] Page source saved: " + sourceFile.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("[WARN] Failed to capture failure diagnostics: " + e.getMessage());
        }
    }

    // --- Explicit Wait Helpers ---

    protected WebElement waitForVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected boolean waitForUrlContains(String fragment) {
        return wait.until(ExpectedConditions.urlContains(fragment));
    }

    protected void safeClick(By locator) {
        waitForClickable(locator).click();
    }

    protected void safeType(By locator, String text) {
        WebElement element = waitForVisible(locator);
        element.clear();
        element.sendKeys(text);
    }

    protected void navigateTo(String path) {
        String url = baseUrl + (path.startsWith("/") ? path : "/" + path);
        driver.get(url);
    }
}
