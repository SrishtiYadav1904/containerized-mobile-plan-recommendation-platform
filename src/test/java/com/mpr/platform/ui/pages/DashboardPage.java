package com.mpr.platform.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page Object for Executive Summary Dashboard page (/dashboard).
 */
public class DashboardPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By pageTitle = By.className("page-title");
    private final By kpiCards = By.className("plan-card");

    public DashboardPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public boolean isDashboardLoaded() {
        try {
            WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(pageTitle));
            return title.getText().contains("Executive Summary Dashboard");
        } catch (Exception e) {
            return false;
        }
    }

    public int getKpiCardsCount() {
        try {
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(kpiCards));
            return driver.findElements(kpiCards).size();
        } catch (Exception e) {
            return 0;
        }
    }
}
