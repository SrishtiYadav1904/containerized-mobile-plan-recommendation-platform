package com.mpr.platform.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page Object for Plan Creation page (/plans/new).
 */
public class PlanCreatePage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By operatorSelect = By.id("operator");
    private final By nameInput = By.id("name");
    private final By priceInput = By.id("price");
    private final By validityDaysInput = By.id("validityDays");
    private final By dataGbPerDayInput = By.id("dataGbPerDay");
    private final By voiceMinutesInput = By.id("voiceMinutes");
    private final By smsCountInput = By.id("smsCount");
    private final By benefitsTextarea = By.id("benefits");
    private final By submitButton = By.cssSelector("form button[type='submit']");
    private final By pageTitle = By.className("page-title");

    public PlanCreatePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public boolean isPageLoaded() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(nameInput)).isDisplayed();
    }

    public void fillAndSubmitPlan(String operator, String name, double price, int validityDays,
                                  double dataGbPerDay, int voiceMinutes, int smsCount, String benefits) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(operatorSelect));

        new Select(driver.findElement(operatorSelect)).selectByValue(operator);

        setInputValue(nameInput, name);
        setInputValue(priceInput, String.format(java.util.Locale.US, "%.2f", price));
        setInputValue(validityDaysInput, String.valueOf(validityDays));
        setInputValue(dataGbPerDayInput, String.format(java.util.Locale.US, "%.1f", dataGbPerDay));
        setInputValue(voiceMinutesInput, String.valueOf(voiceMinutes));
        setInputValue(smsCountInput, String.valueOf(smsCount));

        if (benefits != null && !benefits.isEmpty()) {
            setInputValue(benefitsTextarea, benefits);
        }

        WebElement submitBtn = wait.until(ExpectedConditions.presenceOfElementLocated(submitButton));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", submitBtn);
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", submitBtn);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert-success")));
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
}
