package org.springframework.samples.petclinic.selenium.pages;

import org.openqa.selenium.WebDriver;

public class OwnerDetailsPage {
	private WebDriver driver;

	public OwnerDetailsPage(WebDriver driver) {
		this.driver = driver;
		if (!driver.getCurrentUrl().contains("/owner/")) {
			throw new IllegalStateException("This is not an Owner details page");
		}
	}
}
