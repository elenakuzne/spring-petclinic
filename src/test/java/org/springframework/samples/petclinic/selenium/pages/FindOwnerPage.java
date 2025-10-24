package org.springframework.samples.petclinic.selenium.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class FindOwnerPage {
	protected WebDriver driver;

	@FindBy(linkText = "Add Owner")
	private WebElement addOwnerLink;

	public FindOwnerPage(WebDriver driver) {
		this.driver = driver;
		if (!driver.getCurrentUrl().endsWith("/owners/find")) {
			throw new IllegalStateException("This is not an Owner search page");
		}
	}

	public CreateOwnerFormPage ClickAddOwner() {
		addOwnerLink.click();
		return new CreateOwnerFormPage(driver);
	}
}

