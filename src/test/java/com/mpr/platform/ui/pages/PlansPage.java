package com.mpr.platform.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Page Object for Plan Catalogue page (/plans).
 */
public class PlansPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By pageTitle = By.className("page-title");
    private final By searchInput = By.id("searchQ");
    private final By operatorSelect = By.id("operatorSelect");
    private final By maxPriceInput = By.id("maxPriceInput");
    private final By validitySelect = By.id("validitySelect");
    private final By filterButton = By.cssSelector("form button[type='submit']");
    private final By planCards = By.className("plan-card");
    private final By planNames = By.className("plan-name");
    private final By planOperators = By.className("plan-operator");
    private final By planPrices = By.className("plan-price");
    private final By roleBadge = By.className("badge-role");
    private final By createPlanButton = By.linkText("+ Create Plan");
    private final By newPlanDraftButton = By.linkText("+ New Plan Draft");
    private final By dashboardNavLink = By.linkText("Dashboard");
    private final By logoutButton = By.cssSelector("form[action*='logout'] button");

    public PlansPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public boolean isPageLoaded() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(pageTitle)).isDisplayed();
    }

    public String getCurrentRoleBadge() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(roleBadge)).getText();
        } catch (Exception e) {
            return "";
        }
    }

    public void search(String keyword) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput));
        WebElement input = driver.findElement(searchInput);
        input.clear();
        input.sendKeys(keyword);
        driver.findElement(filterButton).click();
        wait.until(ExpectedConditions.urlContains("q="));
    }

    public void filterByOperator(String operator) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(operatorSelect));
        Select select = new Select(driver.findElement(operatorSelect));
        select.selectByValue(operator);
        driver.findElement(filterButton).click();
        wait.until(ExpectedConditions.urlContains("operator="));
    }

    public void filterByMaxPrice(double maxPrice) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(maxPriceInput));
        WebElement input = driver.findElement(maxPriceInput);
        input.clear();
        input.sendKeys(String.valueOf((int) maxPrice));
        driver.findElement(filterButton).click();
        wait.until(ExpectedConditions.urlContains("maxPrice="));
    }

    public int getPlanCardsCount() {
        try {
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(planCards));
            return driver.findElements(planCards).size();
        } catch (Exception e) {
            return 0;
        }
    }

    public List<String> getAllPlanNames() {
        return driver.findElements(planNames).stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    public List<String> getAllPlanOperators() {
        return driver.findElements(planOperators).stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    public void openPlanDetails(String planName) {
        By cardDetailLink = By.xpath("//div[contains(@class, 'plan-card')][.//h3[contains(text(), '" + planName + "')]]//a[contains(text(), 'View Details')]");
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(cardDetailLink));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center', inline: 'center'});", element);
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element)).click();
        } catch (org.openqa.selenium.ElementClickInterceptedException e) {
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    public boolean hasCreatePlanOption() {
        return !driver.findElements(createPlanButton).isEmpty() || !driver.findElements(newPlanDraftButton).isEmpty();
    }

    public boolean hasDashboardOption() {
        return !driver.findElements(dashboardNavLink).isEmpty();
    }

    public void clickNewPlan() {
        if (!driver.findElements(newPlanDraftButton).isEmpty()) {
            driver.findElement(newPlanDraftButton).click();
        } else {
            driver.findElement(createPlanButton).click();
        }
    }

    public void logout() {
        if (!driver.findElements(logoutButton).isEmpty()) {
            WebElement btn = driver.findElement(logoutButton);
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
            wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(By.linkText("Sign in")),
                ExpectedConditions.urlMatches(".*/$"),
                ExpectedConditions.urlMatches(".*/login.*"),
                ExpectedConditions.not(ExpectedConditions.presenceOfElementLocated(By.className("badge-role")))
            ));
        }
    }
}
