package org.springframework.samples.petclinic.selenium;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.selenium.pages.CreateOwnerFormPage;
import org.springframework.samples.petclinic.selenium.pages.FindOwnerPage;
import org.springframework.samples.petclinic.selenium.pages.OwnerDetailsPage;

public class OwnerCreationTests extends BaseSeleniumTest {
	@Test
	public void testCreateOwnerWithAllValidFields() {
		driver.get(baseUrl + "/owners/find");

		FindOwnerPage findOwnerPage = new FindOwnerPage(driver);
		CreateOwnerFormPage createOwnerFormPage = findOwnerPage.ClickAddOwner();

		// Enter valid owner data
		createOwnerFormPage
			.enterFirstName("First")
			.enterLastName("Last")
			.enterAddress("Addr")
			.enterCity("Cty")
			.enterTelephone("1234567890");

		// Submit form and expect redirect to owner details.
		OwnerDetailsPage ownerDetailsPage = createOwnerFormPage.submit();
	}
}
