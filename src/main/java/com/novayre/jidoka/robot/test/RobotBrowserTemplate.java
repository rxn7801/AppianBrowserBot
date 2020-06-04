package com.novayre.jidoka.robot.test;

import org.apache.commons.lang.StringUtils;

import com.novayre.jidoka.browser.api.EBrowsers;
import com.novayre.jidoka.browser.api.IWebBrowserSupport;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.annotations.Robot;
import com.novayre.jidoka.client.api.multios.IClient;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Browser robot template.
 */
@Robot
public class RobotBrowserTemplate implements IRobot {

    /**
     * URL to navigate to.
     */
    private static final String HOME_URL = "https://wise-pentameter-276312.uc.r.appspot.com";

    /**
     * The JidokaServer instance.
     */
    private IJidokaServer<?> server;

    /**
     * The IClient module.
     */
    private IClient client;

    /**
     * WebBrowser module
     */
    private IWebBrowserSupport browser;

    /**
     * Browser type parameter
     **/
    private String browserType;

    private String lastName;

    private String accountNumber;

    private String sortCode;

    private String postCode;

    private String dateOfBirth;

    private Map<String, String> resultMap = new HashMap<>();

    /**
     * Action "start"
     *
     * @throws Exception
     */
    public void start() throws Exception {

        server = (IJidokaServer<?>) JidokaFactory.getServer();

        client = IClient.getInstance(this);
    }

    /**
     * Open Web Browser
     *
     * @throws Exception
     */
    public void openBrowser() throws Exception {

        lastName = server.getParameters().get("lastName");
        policyNumber = server.getParameters().get("policyNumber");
        postCode = server.getParameters().get("postCode");
        dateOfBirth = server.getParameters().get("dateOfBirth");
        resultMap.put("matchFound", "false");

        browser = IWebBrowserSupport.getInstance(this, client);

        browserType = server.getParameters().get("Browser");

        // Select browser type
        if (StringUtils.isBlank(browserType)) {
            server.info("Browser parameter not present. Using the default browser CHROME");
            browser.setBrowserType(EBrowsers.CHROME);
            browserType = EBrowsers.CHROME.name();
        } else {
            EBrowsers selectedBrowser = EBrowsers.valueOf(browserType);
            browser.setBrowserType(selectedBrowser);
            server.info("Browser selected: " + selectedBrowser.name());
        }

        // Set timeout to 60 seconds
        browser.setTimeoutSeconds(60);

        // Init the browser module
        browser.initBrowser();

        server.setNumberOfItems(1);

    }

    /**
     * Navigate to Web Page
     *
     * @throws Exception
     */
    public void navigateToWeb() throws Exception {

        server.setCurrentItem(1, HOME_URL);

        // Navegate to HOME_URL address
        browser.navigate(HOME_URL);

        //Tihis command is uses to make visible in the desktop the page (IExplore issue)
        if (browserType.equals("IE")) {
            client.clickOnCenter();
            client.pause(3000);
        }

        By lName = By.xpath("/html/body/div/div/div[1]/form/table/tbody/tr[1]/td/div/input");
        By dob = By.xpath("/html/body/div/div/div[1]/form/table/tbody/tr[2]/td/div/input");
        By accNumber = By.xpath("/html/body/div/div/div[1]/form/table/tbody/tr[3]/td/div/input");
        By sortCd = By.xpath("/html/body/div/div/div[1]/form/table/tbody/tr[4]/td/div/input");
        By pCode = By.xpath("/html/body/div/div/div[1]/form/table/tbody/tr[5]/td/div/input");
        browser.textFieldSet(lName,lastName,true);
        browser.textFieldSet(dob,dateOfBirth,true);
        browser.textFieldSet(accNumber,accountNumber,true);
        browser.textFieldSet(sortCd,sortCode,true);
        browser.textFieldSet(pCode,postCode,true);

        browser.clickOnElement(By.xpath("/html/body/div/div/div[1]/form/div/input"));

        By message = By.id("message");

        boolean isMessageExists = browser.existsElement(message);

        if(!isMessageExists) {

            By tableId = By.id("customer");
            WebElement table = browser.getElement(tableId);

            List<WebElement> th = table.findElements(By.tagName("th"));

            int lastNamePos = 0;
            int postCodePos = 0;
            int dobPos = 0;
            int accountNoPos = 0;
            int sortCodePos = 0;

            for (int i = 0; i < th.size(); i++) {
                if ("Last Name".equalsIgnoreCase(th.get(i).getText())) {
                    lastNamePos = i + 1;
                } else if ("Date of Birth".equalsIgnoreCase(th.get(i).getText())) {
                    dobPos = i + 1;
                } else if ("Account Number".equalsIgnoreCase(th.get(i).getText())) {
                    accountNoPos = i + 1;
                } else if ("Sort Code".equalsIgnoreCase(th.get(i).getText())) {
                    sortCodePos = i + 1;
                }  else if ("Post Code".equalsIgnoreCase(th.get(i).getText())) {
                    postCodePos = i + 1;
                }
            }

            List<WebElement> lastNameElements = table.findElements(By.xpath("//tr/td[" + lastNamePos + "]"));
            List<WebElement> dobElements = table.findElements(By.xpath("//tr/td[" + dobPos + "]"));
            List<WebElement> accountElements = table.findElements(By.xpath("//tr/td[" + accountNoPos + "]"));
            List<WebElement> sortCodeElements = table.findElements(By.xpath("//tr/td[" + sortCodePos + "]"));
            List<WebElement> postCodeElements = table.findElements(By.xpath("//tr/td[" + postCodePos + "]"));

            for (int i = 0; i < lastNameElements.size(); i++) {
                WebElement e = lastNameElements.get(i);
                if (e.getText().trim().equalsIgnoreCase(lastName)) {
                    if (dobElements.get(i).getText().trim().equalsIgnoreCase(dateOfBirth)
                            && accountElements.get(i).getText().trim().equalsIgnoreCase(accountNumber)
                            && sortCodeElements.get(i).getText().trim().equalsIgnoreCase(sortCode)
                            && postCodeElements.get(i).getText().trim().equalsIgnoreCase(postCode)) {
                        resultMap.put("matchFound", "true");
                        break;
                    }
                }
            }
        } else
            resultMap.put("matchFound", "false");


    }

    /**
     * We use the close method implemented in the driver.
     * In your robots you should use browser.close()
     */
    private void close() throws Exception {
        browser.getDriver().close();
    }

    /**
     * Close Browser
     *
     * @throws Exception
     */
    public void closeBrowser() throws Exception {
        close();
        server.setCurrentItemResultToOK("Success");
    }


    /**
     * Action "end"
     *
     * @throws Exception
     */
    public void end() throws Exception {
        server.setResultProperties(resultMap);
    }


}
