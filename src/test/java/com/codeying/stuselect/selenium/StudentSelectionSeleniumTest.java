package com.codeying.stuselect.selenium;

import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Selenium test for the student course selection workflow.
 */
public class StudentSelectionSeleniumTest {

  private static final String BASE_URL = "http://127.0.0.1:8081/";
  private static final String JDBC_URL =
      "jdbc:mysql://127.0.0.1:3306/app_stu_select"
          + "?serverTimezone=Asia/Shanghai"
          + "&characterEncoding=utf-8"
          + "&allowPublicKeyRetrieval=true"
          + "&useSSL=false";
  private static final String DB_USER = "app_stu_select";
  private static final String DB_PASSWORD = "app_stu_select";
  private static final String STUDENT_ID = "S3003";
  private static final String COURSE_ID = "C4012";
  private static final String COURSE_LABEL = "音乐欣赏 / 张若琳 / 周四第7-8节 / 100人";

  private WebDriver driver;
  private WebDriverWait wait;

  /**
   * Creates a browser session and prepares repeatable test data.
   *
   * @throws SQLException when database cleanup fails
   */
  @Before
  public void setUp() throws SQLException {
    resetSelectionData();
    driver = new ChromeDriver();
    driver.manage().window().setSize(new Dimension(1440, 1000));
    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
    wait = new WebDriverWait(driver, Duration.ofSeconds(10));
  }

  /**
   * Verifies the student login and course selection flow.
   */
  @Test
  public void testStudentCreateSelectionFlow() {
    driver.get(BASE_URL);
    wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[placeholder='请输入账号']")))
        .sendKeys("stu_qin");
    driver.findElement(By.cssSelector("input[placeholder='请输入密码']"))
        .sendKeys("123456");
    new Select(driver.findElement(By.cssSelector("form.auth-form select")))
        .selectByValue("student");
    click(By.xpath("//button[contains(.,'进入学生选课系统')]"));

    String loginMessage =
        wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".message.success")))
            .getText();
    assertTrue(loginMessage.contains("登录成功"));

    click(By.xpath("//strong[normalize-space()='选课记录']"));
    click(By.xpath("//button[contains(.,'发起选课')]"));
    new Select(
            wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".modal-card select"))))
        .selectByVisibleText(COURSE_LABEL);
    click(By.xpath("//button[contains(.,'保存变更')]"));

    assertTrue(
        wait.until(
            ExpectedConditions.textToBePresentInElementLocated(
                By.cssSelector(".message.success.inline"),
                "保存成功")));
  }

  /**
   * Closes the browser session after the test.
   */
  @After
  public void tearDown() {
    if (driver != null) {
      driver.quit();
    }
  }

  private void resetSelectionData() throws SQLException {
    try (Connection connection =
        DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD)) {
      try (PreparedStatement statement =
          connection.prepareStatement(
              "delete from tb_sct where studentId = ? and courseid = ?")) {
        statement.setString(1, STUDENT_ID);
        statement.setString(2, COURSE_ID);
        statement.executeUpdate();
      }
      try (PreparedStatement statement =
          connection.prepareStatement(
              "update tb_selection_window "
                  + "set start_time = '2026-06-01 00:00:00', "
                  + "end_time = '2026-12-31 23:59:59', "
                  + "enabled = 1 where action_type = 'SELECT'")) {
        statement.executeUpdate();
      }
    }
  }

  private void click(final By locator) {
    final WebElement element =
        wait.until(ExpectedConditions.elementToBeClickable(locator));
    ((JavascriptExecutor) driver)
        .executeScript(
            "arguments[0].scrollIntoView({block: 'center'});",
            element);
    wait.until(ExpectedConditions.elementToBeClickable(locator));
    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
  }
}
