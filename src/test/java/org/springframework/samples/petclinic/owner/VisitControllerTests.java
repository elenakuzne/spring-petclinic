/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.owner;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Nested;

/**
 * Test class for {@link VisitController}
 *
 * @author Colin But
 * @author Wick Dynex
 */
@WebMvcTest(VisitController.class)
@DisabledInNativeImage
@DisabledInAotMode
class VisitControllerTests {

	private static final int TEST_OWNER_ID = 1;

	private static final int TEST_PET_ID = 1;

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OwnerRepository owners;

	@BeforeEach
	void init() {
		Owner owner = new Owner();
		Pet pet = new Pet();
		owner.addPet(pet);
		pet.setId(TEST_PET_ID);
		given(this.owners.findById(TEST_OWNER_ID)).willReturn(Optional.of(owner));
	}

	@Test
	void testInitNewVisitForm() throws Exception {
		mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID))
			.andExpect(status().isOk())
			.andExpect(view().name("pets/createOrUpdateVisitForm"));
	}

	@Test
	void testProcessNewVisitFormSuccess() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
				.param("name", "George")
				.param("description", "Visit Description"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/{ownerId}"));
	}

	@Test
	void testProcessNewVisitFormHasErrors() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID).param("name",
					"George"))
			.andExpect(model().attributeHasErrors("visit"))
			.andExpect(status().isOk())
			.andExpect(view().name("pets/createOrUpdateVisitForm"));
	}

	/**
	 * Additional tests for Visit date validation and edge cases
	 */
	@Nested
	class VisitDateValidationTests {

		@Test
		void testProcessNewVisitFormWithExplicitDate() throws Exception {
			mockMvc
				.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
					.param("date", LocalDate.now().toString())
					.param("description", "Regular checkup"))
				.andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:/owners/{ownerId}"));
		}

		@Test
		void testProcessNewVisitFormWithPastDate() throws Exception {
			LocalDate pastDate = LocalDate.now().minusDays(30);
			mockMvc
				.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
					.param("date", pastDate.toString())
					.param("description", "Past visit"))
				.andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:/owners/{ownerId}"));
		}

		@Test
		void testProcessNewVisitFormWithInvalidDateFormat() throws Exception {
			mockMvc
				.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
					.param("date", "2024/01/15")
					.param("description", "Invalid date format"))
				.andExpect(status().isOk())
				.andExpect(model().attributeHasErrors("visit"))
				.andExpect(view().name("pets/createOrUpdateVisitForm"));
		}

		@Test
		void testProcessNewVisitFormWithEmptyDate() throws Exception {
			mockMvc
				.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID).param("date", "")
					.param("description", "Visit without date"))
				.andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:/owners/{ownerId}"));
		}

	}

	/**
	 * Additional tests for Visit description validation
	 */
	@Nested
	class VisitDescriptionValidationTests {

		@Test
		void testProcessNewVisitFormWithLongDescription() throws Exception {
			String longDescription = "A".repeat(500);
			mockMvc
				.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
					.param("description", longDescription))
				.andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:/owners/{ownerId}"));
		}

		@Test
		void testProcessNewVisitFormWithEmptyStringDescription() throws Exception {
			mockMvc
				.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
					.param("description", ""))
				.andExpect(status().isOk())
				.andExpect(model().attributeHasErrors("visit"))
				.andExpect(model().attributeHasFieldErrors("visit", "description"))
				.andExpect(view().name("pets/createOrUpdateVisitForm"));
		}

		@Test
		void testProcessNewVisitFormWithWhitespaceDescription() throws Exception {
			mockMvc
				.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
					.param("description", "   \t\n   "))
				.andExpect(status().isOk())
				.andExpect(model().attributeHasErrors("visit"))
				.andExpect(model().attributeHasFieldErrors("visit", "description"))
				.andExpect(view().name("pets/createOrUpdateVisitForm"));
		}

		@Test
		void testProcessNewVisitFormWithSpecialCharacters() throws Exception {
			mockMvc
				.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
					.param("description", "Visit for @#$% symptoms & treatment!"))
				.andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:/owners/{ownerId}"));
		}

	}

	/**
	 * Tests for edge cases and error scenarios
	 */
	@Nested
	class VisitEdgeCaseTests {

		@Test
		void testProcessNewVisitFormWithAllFieldsPopulated() throws Exception {
			mockMvc
				.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
					.param("date", LocalDate.now().minusDays(5).toString())
					.param("description", "Complete visit information with all details"))
				.andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:/owners/{ownerId}"));
		}

	}

}
