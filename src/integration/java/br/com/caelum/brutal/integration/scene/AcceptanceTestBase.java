package br.com.caelum.brutal.integration.scene;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Navigation;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import br.com.caelum.brutal.integration.pages.Home;
import br.com.caelum.brutal.integration.util.AppMessages;
import br.com.caelum.brutal.integration.util.ServerInfo;
import br.com.caelum.vraptor.environment.DefaultEnvironment;
import br.com.caelum.vraptor.environment.Environment;

import com.gargoylesoftware.htmlunit.BrowserVersion;

public abstract class AcceptanceTestBase implements ServerInfo.AcceptanceTest {

	private static final int TIME_WAIT = 5;

	protected static WebDriver driver;

	protected static HttpClient client;
	
	protected static Environment env;
	
	private AppMessages messages = new AppMessages();
	
	@AfterClass
	public static void close() {
		if (driver != null)
			driver.close();
	}

	private static WebDriver htmlUnitDriver() {
		HtmlUnitDriver driver = new HtmlUnitDriver(BrowserVersion.FIREFOX_3_6);
		driver.setJavascriptEnabled(true);
		return driver;
	}
	
	private static WebDriver ghostDriver() {
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setJavascriptEnabled(true);
		try {
			return new RemoteWebDriver(new URL("http://localhost:8787/"), capabilities);
		} catch (MalformedURLException e) {
			throw new RuntimeException("could not build ghost driver", e);
		}
	}

	protected Navigation browser() {
		return driver.navigate();
	}
	
	protected Home home() {
		return new Home(driver);
	}

	@Before
	public void setUpEnv() {
		client = new HttpClient();
	}
	
	@Before
	public void setUpImplicitWait() {
		driver.manage().timeouts().implicitlyWait(implicitWaitSeconds(), TimeUnit.SECONDS);
	}
	

	@BeforeClass
	public static void buildDriver() {
//		System.setProperty("webdriver.chrome.driver", "/home/csokol/downloads/chromedriver");
//		driver = new ChromeDriver();
//		driver = ghostDriver();
		driver = new FirefoxDriver();
		waitForFirstBodyPresence();
	}
	
	public static WebDriver getDriver() {
		return driver;
	}
	
	@BeforeClass
	public static void getHttpClient() {
	    client = new HttpClient();
	    String homeUri = SERVER.urlFor("");
	    try {
	        HttpMethod method = new GetMethod(homeUri);
	        int status = client.executeMethod(method);
	        int digit = status % 100;
	        if (digit == 5 || digit == 4) {
	            throw new RuntimeException("server responded with "+ status + " status for a GET request to uri: " + homeUri + ", is the server ok?");
	        }
	    } catch (IOException e) {
	        throw new RuntimeException("could not execute GET to: " + homeUri + ", is the server up?", e);
	    }
	}
	
	@BeforeClass
	public static void getEnv() throws IOException {
	    String homologEnv = System.getenv("ACCEPTANCE_ENV");
        if (homologEnv == null) {
            homologEnv = "development";
        }
        env = new DefaultEnvironment(homologEnv);
	}

    private static void waitForFirstBodyPresence() {
		driver.get(SERVER.urlFor(""));
		ExpectedCondition<WebElement> homeAppear = new ExpectedCondition<WebElement>() {
			@Override
			public WebElement apply(WebDriver d) {
				return d.findElement(By.tagName("body"));
			}
		};
		new WebDriverWait(driver, 40).until(homeAppear);
	}
    
    protected String message(String text) {
        return messages.getMessage(text);
    }
    
    protected void createQuestion() {
		home().toNewQuestionPage()
            .newQuestion("question title question title question title", 
                "question description question description question description question description ", 
                "java");
	}
    
    public int implicitWaitSeconds() {
    	return TIME_WAIT;
    }
    
}
