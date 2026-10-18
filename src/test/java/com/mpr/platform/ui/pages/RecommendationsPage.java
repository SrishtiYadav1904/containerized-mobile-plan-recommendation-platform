package com.mpr.platform.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Page Object for Recommendations form and results page (/recommendations).
 */
public class RecommendationsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By monthlyDataInput = By.id("monthlyDataGb");
    private final By monthlyMinutesInput = By.id("monthlyMinutes");
    private final By budgetInput = By.id("budget");
    private final By preferencesInput = By.id("preferences");
    private final By submitButton = By.cssSelector("form button[type='submit']");
    private final By resultsTitle = By.className("page-title");
    private final By planNames = By.className("plan-name");
    private final By scorePills = By.className("score-badge");

    public RecommendationsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public boolean isFormLoaded() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(monthlyDataInput)).isDisplayed();
    }

    public void submitRecommendationRequest(double monthlyDataGb, int monthlyMinutes, double budget, String preferences) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(monthlyDataInput));

        WebElement dataElem = driver.findElement(monthlyDataInput);
        dataElem.clear();
        dataElem.sendKeys(String.valueOf(monthlyDataGb));

        WebElement minsElem = driver.findElement(monthlyMinutesInput);
        minsElem.clear();
        minsElem.sendKeys(String.valueOf(monthlyMinutes));

        WebElement budgetElem = driver.findElement(budgetInput);
        budgetElem.clear();
        budgetElem.sendKeys(String.valueOf((int) budget));

        if (preferences != null && !preferences.isEmpty()) {
            WebElement prefElem = driver.findElement(preferencesInput);
            prefElem.clear();
            prefElem.sendKeys(preferences);
        }

        driver.findElement(submitButton).click();
    }

    public boolean isResultsHeaderDisplayed() {
        try {
            WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(resultsTitle));
            return title.getText().contains("Top Recommended") || title.getText().contains("Recommendations");
        } catch (Exception e) {
            return false;
        }
    }

    public List<String> getRecommendedPlanNames() {
        try {
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(planNames));
            return driver.findElements(planNames).stream()
                    .map(WebElement::getText)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }
}
