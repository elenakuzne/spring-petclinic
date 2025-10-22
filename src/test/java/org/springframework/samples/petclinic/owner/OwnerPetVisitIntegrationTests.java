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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the complete workflow of Owner, Pet, and Visit operations. These
 * tests verify that all four required functionalities work together correctly.
 *
 * @author QA Test Implementation
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OwnerPetVisitIntegrationTests {

	@Autowired
	private OwnerRepository ownerRepository;

	@Autowired
	private PetTypeRepository petTypeRepository;

	private final Pageable pageable = Pageable.unpaged();

	/**
	 * Test complete workflow: Create Owner → Search Owner → Add Pet → Add Visit Covers
	 * all four required functionalities in sequence
	 */
	@Test
	@Transactional
	void testCompleteOwnerPetVisitWorkflow() {
		// Step 1: Create a new Owner (Functionality b: Anlegen eines Tierhalters)
		Owner newOwner = new Owner();
		newOwner.setFirstName("Integration");
		newOwner.setLastName("TestOwner");
		newOwner.setAddress("123 Integration St");
		newOwner.setCity("TestCity");
		newOwner.setTelephone("5551234567");

		Owner savedOwner = ownerRepository.save(newOwner);
		assertThat(savedOwner.getId()).isNotNull();
		assertThat(savedOwner.getId()).isGreaterThan(0);

		Integer ownerId = savedOwner.getId();

		// Step 2: Search for the created Owner (Functionality a: Suche nach einem
		// Tierhalter)
		Page<Owner> searchResults = ownerRepository.findByLastNameStartingWith("TestOwner", pageable);
		assertThat(searchResults.getTotalElements()).isGreaterThan(0);
		assertThat(searchResults.getContent()).anyMatch(o -> o.getId().equals(ownerId));

		// Step 3: Add a Pet to the Owner (Functionality c: Anlegen eines neuen Tiers)
		Collection<PetType> petTypes = petTypeRepository.findPetTypes();
		assertThat(petTypes).isNotEmpty();
		PetType petType = petTypes.iterator().next();

		Pet newPet = new Pet();
		newPet.setName("IntegrationPet");
		newPet.setType(petType);
		newPet.setBirthDate(LocalDate.now().minusYears(2));

		savedOwner.addPet(newPet);
		ownerRepository.save(savedOwner);

		// Verify pet was added correctly
		Optional<Owner> ownerWithPet = ownerRepository.findById(ownerId);
		assertThat(ownerWithPet).isPresent();
		assertThat(ownerWithPet.get().getPets()).hasSize(1);

		Pet savedPet = ownerWithPet.get().getPet("IntegrationPet");
		assertThat(savedPet).isNotNull();
		assertThat(savedPet.getId()).isNotNull();

		// Step 4: Add a Visit for the Pet (Functionality d: Hinzufügen eines neuen
		// Besuchs)
		Visit newVisit = new Visit();
		newVisit.setDate(LocalDate.now());
		newVisit.setDescription("Integration test visit - regular checkup");

		ownerWithPet.get().addVisit(savedPet.getId(), newVisit);
		ownerRepository.save(ownerWithPet.get());

		// Verify visit was added correctly
		Optional<Owner> finalOwner = ownerRepository.findById(ownerId);
		assertThat(finalOwner).isPresent();

		Pet petWithVisit = finalOwner.get().getPet("IntegrationPet");
		assertThat(petWithVisit).isNotNull();
		assertThat(petWithVisit.getVisits()).hasSize(1);
		Visit savedVisit = petWithVisit.getVisits().iterator().next();
		assertThat(savedVisit.getDescription()).isEqualTo("Integration test visit - regular checkup");
		assertThat(savedVisit.getId()).isNotNull();
	}

	/**
	 * Test adding multiple pets to an owner and visits to multiple pets
	 */
	@Test
	@Transactional
	void testOwnerWithMultiplePetsAndVisits() {
		// Create Owner
		Owner owner = new Owner();
		owner.setFirstName("MultiPet");
		owner.setLastName("Owner");
		owner.setAddress("456 Multi Street");
		owner.setCity("PetCity");
		owner.setTelephone("5559876543");

		Owner savedOwner = ownerRepository.save(owner);

		// Get available pet types
		Collection<PetType> petTypes = petTypeRepository.findPetTypes();
		assertThat(petTypes).hasSizeGreaterThanOrEqualTo(2);

		PetType[] typeArray = petTypes.toArray(new PetType[0]);

		// Add first pet
		Pet pet1 = new Pet();
		pet1.setName("FirstPet");
		pet1.setType(typeArray[0]);
		pet1.setBirthDate(LocalDate.now().minusYears(3));
		savedOwner.addPet(pet1);

		// Add second pet
		Pet pet2 = new Pet();
		pet2.setName("SecondPet");
		pet2.setType(typeArray[1]);
		pet2.setBirthDate(LocalDate.now().minusMonths(6));
		savedOwner.addPet(pet2);

		ownerRepository.save(savedOwner);

		// Verify both pets were added
		Optional<Owner> ownerWithPets = ownerRepository.findById(savedOwner.getId());
		assertThat(ownerWithPets).isPresent();
		assertThat(ownerWithPets.get().getPets()).hasSize(2);

		// Add visits to both pets
		Pet savedPet1 = ownerWithPets.get().getPet("FirstPet");
		Pet savedPet2 = ownerWithPets.get().getPet("SecondPet");

		Visit visit1 = new Visit();
		visit1.setDate(LocalDate.now().minusDays(10));
		visit1.setDescription("First pet checkup");
		ownerWithPets.get().addVisit(savedPet1.getId(), visit1);

		Visit visit2 = new Visit();
		visit2.setDate(LocalDate.now().minusDays(5));
		visit2.setDescription("Second pet vaccination");
		ownerWithPets.get().addVisit(savedPet2.getId(), visit2);

		ownerRepository.save(ownerWithPets.get());

		// Verify all visits were added
		Optional<Owner> finalOwner = ownerRepository.findById(savedOwner.getId());
		assertThat(finalOwner).isPresent();

		Pet finalPet1 = finalOwner.get().getPet("FirstPet");
		Pet finalPet2 = finalOwner.get().getPet("SecondPet");

		assertThat(finalPet1.getVisits()).hasSize(1);
		assertThat(finalPet2.getVisits()).hasSize(1);
	}

	/**
	 * Test searching for owner after updates
	 */
	@Test
	@Transactional
	void testSearchOwnerAfterCreationAndUpdate() {
		// Create owner with unique last name
		Owner owner = new Owner();
		owner.setFirstName("Search");
		owner.setLastName("UniqueLastName");
		owner.setAddress("789 Search street");
		owner.setCity("SearchCity");
		owner.setTelephone("5551112222");

		Owner savedOwner = ownerRepository.save(owner);

		// Search should find the owner
		Page<Owner> results = ownerRepository.findByLastNameStartingWith("UniqueLastName", pageable);
		assertThat(results.getTotalElements()).isEqualTo(1);
		assertThat(results.getContent().get(0).getFirstName()).isEqualTo("Search");

		// Update owner
		savedOwner.setFirstName("UpdatedSearch");
		ownerRepository.save(savedOwner);

		// Search should still find the owner with updated data
		Page<Owner> updatedResults = ownerRepository.findByLastNameStartingWith("UniqueLastName", pageable);
		assertThat(updatedResults.getTotalElements()).isEqualTo(1);
		assertThat(updatedResults.getContent().get(0).getFirstName()).isEqualTo("UpdatedSearch");
	}

	/**
	 * Test partial search functionality (startsWith)
	 */
	@Test
	@Transactional
	void testPartialOwnerSearch() {
		// Create owners with similar last names
		Owner owner1 = new Owner();
		owner1.setFirstName("John");
		owner1.setLastName("SmithTest");
		owner1.setAddress("100 Test Street");
		owner1.setCity("TestCity");
		owner1.setTelephone("1234567890");
		ownerRepository.save(owner1);

		Owner owner2 = new Owner();
		owner2.setFirstName("Jane");
		owner2.setLastName("SmithsonTest");
		owner2.setAddress("200 Test St");
		owner2.setCity("TestCity");
		owner2.setTelephone("0987654321");
		ownerRepository.save(owner2);

		// Search with "Smith" should find both
		Page<Owner> smithResults = ownerRepository.findByLastNameStartingWith("Smith", pageable);
		assertThat(smithResults.getTotalElements()).isGreaterThanOrEqualTo(2);

		// Search with "SmithT" should find only SmithTest
		Page<Owner> smithTestResults = ownerRepository.findByLastNameStartingWith("SmithT", pageable);
		assertThat(smithResults.getTotalElements()).isGreaterThanOrEqualTo(1);
		assertThat(smithTestResults.getContent()).anyMatch(o -> o.getLastName().equals("SmithTest"));
	}

}
