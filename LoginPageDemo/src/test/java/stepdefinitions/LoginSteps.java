package stepdefinitions;

import io.cucumber.java.en.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import pages.LoginPage;
import org.testng.Assert;
import java.util.List;

import java.time.Duration;

public class
LoginSteps {
    WebDriver driver;
    LoginPage loginPage;

    @Given("I am on the login page")
    public void i_am_on_the_login_page() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.get("https://sts.vecvnet.com/adfs/ls/?client-request-id=fe4f8dc5-9e32-48a2-a7d9-9ed6f43a6ec4&wa=wsignin1.0&wtrealm=urn%3afederation%3aMicrosoftOnline&wctx=LoginOptions%3D3%26estsredirect%3d2%26estsrequest%3drQQIARAA42Kw0skoKSkottLXNzbRszTWMzKw0DM0M9IvT01KLCjQT0otSSwtKMnMTS0uzi9MLBLiEjja6__PaN4ij-U35137YpV3ZBWjDV4jcvLTM_P0ixNzc4z0gYagG3mIUc0xKDAp2dLU2DRJN9XM1ELXJNnUTDfR3Mxc19TM0tTMyCgtyTTJ_AIj4wtGxltMrMEgozYxq5ibJVommiQb6KYamyTpmqQmWugmGSSZ61qapSWbGANJy1SzXcwqBgYWiZYmiZa6yclGhrrmlslpupYmlia6BhYpqeaGqRYWpkamj5jFIE4qLU4tcihLTS7LSy3RS87PvcDC84qFx4DZioODS4BBgkGB4QcL4yJWYDA4zVf4Ed0j5tB29mfDnnJNhlOs-uaBnmkRiUYlAaamTnk--ZEFvo5eyRYmZsGm5cEWoakBReY5fj65eeZRoZG2ZlaGE9h4T7ExfGBj6mBnmMXOsIuTonA8wMvwg2_jy65Ty2_8fufxil-nMifJxLLAyCnENdvY3MUys9TLPzfJuTS7JCqiKNmv3MutODAotcoxwzEi1HaDAMMDIBJk-CHIAAA1&cbcxt=&username=uptimeuser%40vecvnet.com&mkt=&lc="); // replace with actual URL
        driver.manage().window().maximize();
        loginPage = new LoginPage(driver);
    }

    @When("I enter {string} and {string}")
    public void i_enter_credentials(String email, String password) {
        loginPage.enterEmail(email);
        loginPage.enterPassword(password);
    }

    @When("I click Sign in")
    public void i_click_sign_in() {
        loginPage.clickSignIn();
    }

    @Then("I should be logged in successfully")
    public void i_should_be_logged_in_successfully() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        try {
            // Handle multiple possible post-login dialogs in sequence
            handlePostLoginDialogs(wait);

            // Final verification - wait for home page
            wait.until(ExpectedConditions.urlContains("home"));
            System.out.println("Successfully logged in, reached Home page!");

        } catch (Exception e) {
            System.out.println("Login verification failed: " + e.getMessage());
            throw new AssertionError("Login was not successful: " + e.getMessage());
        }
    }

    private void handlePostLoginDialogs(WebDriverWait wait) {
        // Try to handle dialogs for up to 60 seconds total
        long endTime = System.currentTimeMillis() + 60000;

        while (System.currentTimeMillis() < endTime) {
            try {
                // Check if we've reached the home page
                if (driver.getCurrentUrl().contains("home")) {
                    System.out.println("Reached home page successfully");
                    break;
                }

                boolean dialogHandled = false;

                // Handle "Do you trust this app?" dialog
                dialogHandled |= handleTrustAppDialog(wait);

                // Handle "Stay signed in?" dialog
                dialogHandled |= handleStaySignedInDialog(wait);

                // Handle any other continue dialogs
                dialogHandled |= handleContinueDialog(wait);

                // If no dialog was handled, wait a bit and check again
                if (!dialogHandled) {
                    Thread.sleep(1000);
                }

            } catch (Exception e) {
                System.out.println("Error handling post-login dialogs: " + e.getMessage());
                break;
            }
        }
    }

    private boolean handleTrustAppDialog(WebDriverWait wait) {
        try {
            // Look for specific trust app dialog elements
            List<WebElement> trustElements = driver.findElements(By.xpath(
                    "//*[contains(text(), 'Do you trust this app?') or contains(text(), 'trust this app')]"
            ));

            if (!trustElements.isEmpty()) {
                System.out.println("Found 'Do you trust this app?' dialog");

                // Try multiple selector strategies for the continue/accept button
                String[] trustButtonSelectors = {
                        "//input[@value='Continue']",
                        "//button[contains(text(), 'Continue')]",
                        "//input[@type='submit' and contains(@id, 'continue')]",
                        "//button[@type='submit']",
                        "//*[@role='button' and contains(text(), 'Continue')]"
                };

                return clickFirstAvailableElement(wait, trustButtonSelectors, "Trust app Continue button");
            }
        } catch (Exception e) {
            System.out.println("Error checking for trust app dialog: " + e.getMessage());
        }
        return false;
    }

    private boolean handleStaySignedInDialog(WebDriverWait wait) {
        try {
            // Look for specific stay signed in dialog elements
            List<WebElement> staySignedElements = driver.findElements(By.xpath(
                    "//*[contains(text(), 'Stay signed in?') or contains(text(), 'stay signed in')]"
            ));

            if (!staySignedElements.isEmpty()) {
                System.out.println("Found 'Stay signed in?' dialog");

                // Try multiple selector strategies for the Yes button
                String[] yesButtonSelectors = {
                        "//input[@value='Yes']",
                        "//button[contains(text(), 'Yes')]",
                        "//input[@type='submit' and contains(@id, 'yes')]",
                        "//button[contains(@class, 'yes') or contains(@id, 'yes')]",
                        "//*[@role='button' and contains(text(), 'Yes')]"
                };

                return clickFirstAvailableElement(wait, yesButtonSelectors, "Stay signed in Yes button");
            }
        } catch (Exception e) {
            System.out.println("Error checking for stay signed in dialog: " + e.getMessage());
        }
        return false;
    }

    private boolean handleContinueDialog(WebDriverWait wait) {
        try {
            // Look for generic continue buttons (but avoid ones we've already handled)
            String[] continueSelectors = {
                    "//button[contains(text(), 'Continue') and not(ancestor::*[contains(text(), 'Do you trust')])]",
                    "//input[@value='Continue' and not(ancestor::*[contains(text(), 'Do you trust')])]"
            };

            return clickFirstAvailableElement(wait, continueSelectors, "Generic Continue button");
        } catch (Exception e) {
            System.out.println("Error checking for continue dialog: " + e.getMessage());
        }
        return false;
    }

    private boolean clickFirstAvailableElement(WebDriverWait wait, String[] selectors, String elementDescription) {
        for (String selector : selectors) {
            try {
                WebElement element = wait.until(
                        ExpectedConditions.elementToBeClickable(By.xpath(selector))
                );

                // Scroll to element if needed
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
                Thread.sleep(500); // Brief pause after scrolling

                element.click();
                System.out.println("Successfully clicked: " + elementDescription + " using selector: " + selector);

                // Wait a moment for the page to respond
                Thread.sleep(2000);
                return true;

            } catch (Exception e) {
                // Continue to next selector
                continue;
            }
        }
        return false;
    }
        @Then("I should see an error message")
    public void i_should_see_an_error_message() {
        Assert.assertTrue(driver.getPageSource().contains("error")
                || driver.getPageSource().contains("invalid"));
        driver.quit();
    }
}