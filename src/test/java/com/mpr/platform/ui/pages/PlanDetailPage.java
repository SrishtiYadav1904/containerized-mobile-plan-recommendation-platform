package com.mpr.platform.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page Object for Plan Details page (/plans/{id}).
 */
public class PlanDetailPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By planTitle = By.className("page-title");
    private final By statusBadge = By.className("badge-status");
    private final By submitForReviewButton = By.xpath("//button[contains(text(), 'Submit for Review')]");
    private final By approvePublishButton = By.xpath("//button[contains(text(), 'Approve & Publish')]");

    public PlanDetailPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public boolean isDetailLoaded() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(planTitle)).isDisplayed();
    }

    public String getStatus() {
        String text = wait.until(ExpectedConditions.visibilityOfElementLocated(statusBadge)).getText().trim();
        if (text.equalsIgnoreCase("In Review") || text.equalsIgnoreCase("IN_REVIEW")) {
            return "IN_REVIEW";
        }
        if (text.equalsIgnoreCase("Published")) {
            return "PUBLISHED";
        }
        if (text.equalsIgnoreCase("Draft")) {
            return "DRAFT";
        }
        if (text.equalsIgnoreCase("Archived")) {
            return "ARCHIVED";
        }
        return text.toUpperCase();
    }

    public boolean canSubmitForReview() {
        return !driver.findElements(submitForReviewButton).isEmpty();
    }

    public void submitForReview() {
        wait.until(ExpectedConditions.elementToBeClickable(submitForReviewButton)).click();
        wait.until(ExpectedConditions.or(
            ExpectedConditions.textToBePresentInElementLocated(statusBadge, "In Review"),
            ExpectedConditions.textToBePresentInElementLocated(statusBadge, "IN_REVIEW"),
            ExpectedConditions.visibilityOfElementLocated(By.className("alert-success"))
        ));
    }

    public boolean canApproveAndPublish() {
        return !driver.findElements(approvePublishButton).isEmpty();
    }

    public void approveAndPublish() {
        wait.until(ExpectedConditions.elementToBeClickable(approvePublishButton)).click();
        wait.until(ExpectedConditions.or(
            ExpectedConditions.textToBePresentInElementLocated(statusBadge, "Published"),
            ExpectedConditions.textToBePresentInElementLocated(statusBadge, "PUBLISHED"),
            ExpectedConditions.visibilityOfElementLocated(By.className("alert-success"))
        ));
    }
}
