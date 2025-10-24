package org.springframework.samples.petclinic.selenium.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class CreateOwnerFormPage {
	private WebDriver driver;

	@FindBy(id = "firstName")
	private WebElement firstNameInput;

	@FindBy(id = "lastName")
	private WebElement lastNameInput;

	@FindBy(id = "address")
	private WebElement addressInput;

	@FindBy(id = "city")
	private WebElement cityInput;

	@FindBy(id = "telephone")
	private WebElement telephoneInput;

	@FindBy(css = "button[type='submit']")
	private WebElement submitButton;

	public CreateOwnerFormPage(WebDriver driver) {
		this.driver = driver;
	}

	public CreateOwnerFormPage enterFirstName(String firstName) {
		firstNameInput.clear();
		firstNameInput.sendKeys(firstName);
		return this;
	}

	public CreateOwnerFormPage enterLastName(String lastName) {
		lastNameInput.clear();
		lastNameInput.sendKeys(lastName);
		return this;
	}

	public CreateOwnerFormPage enterAddress(String address) {
		addressInput.clear();
		addressInput.sendKeys(address);
		return this;
	}

	public CreateOwnerFormPage enterCity(String city) {
		cityInput.clear();
		cityInput.sendKeys(city);
		return this;
	}

	public CreateOwnerFormPage enterTelephone(String telephone) {
		telephoneInput.clear();
		telephoneInput.sendKeys(telephone);
		return this;
	}

	public OwnerDetailsPage submit() {
		submitButton.click();
		return new OwnerDetailsPage(driver);
	}
}
