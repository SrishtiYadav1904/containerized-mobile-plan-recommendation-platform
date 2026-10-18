package com.mpr.platform.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page Object for Sign In page (/login).
 */
public class LoginPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By usernameInput = By.id("username");
    private final By passwordInput = By.id("password");
    private final By submitButton = By.cssSelector("button[type='submit']");
    private final By errorMessageAlert = By.cssSelector(".form-error, .alert-error");
    private final By brandTitle = By.className("brand-title");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void login(String username, String password) {
        setInputValue(usernameInput, username);
        setInputValue(passwordInput, password);

        WebElement submitBtn = wait.until(ExpectedConditions.presenceOfElementLocated(submitButton));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", submitBtn);
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", submitBtn);
        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("/plans"),
            ExpectedConditions.visibilityOfElementLocated(By.className("badge-role")),
            ExpectedConditions.visibilityOfElementLocated(errorMessageAlert)
        ));
    }

    private void setInputValue(By locator, String value) {
        WebElement elem = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        elem.clear();
        elem.sendKeys(value);
        if (!value.equals(elem.getAttribute("value"))) {
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                    "arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('input', {bubbles:true})); arguments[0].dispatchEvent(new Event('change', {bubbles:true}));",
                    elem, value);
        }
    }

    public boolean isErrorMessageDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessageAlert)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isPageLoaded() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(usernameInput)).isDisplayed();
    }
}
