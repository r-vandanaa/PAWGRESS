package com.digitalpet.service;

import com.digitalpet.model.DigitalPet;
import com.digitalpet.model.EvolutionStage;
import com.digitalpet.model.PetMood;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DigitalPetDAO.
 * Tests CRUD operations and data persistence for digital pets.
 */
class DigitalPetDAOTest {
    
    @TempDir
    Path tempDir;
    
    private LocalDataStorage storage;
    private DigitalPetDAO petDAO;
    
    @BeforeEach
    void setUp() {
        System.setProperty("user.home", tempDir.toString());
        storage = new LocalDataStorage();
        petDAO = new DigitalPetDAO(storage);
    }
    
    @AfterEach
    void tearDown() {
        if (storage != null) {
            storage.close();
        }
    }
    
    @Test
    @DisplayName("Save new pet creates record in database")
    void testSaveNewPet() throws SQLException {
        DigitalPet pet = new DigitalPet("TestPet");
        pet.setExperiencePoints(150);
        pet.setCurrentMood(PetMood.HAPPY);
        
        assertDoesNotThrow(() -> petDAO.save(pet));
        
        // Verify pet was saved
        Optional<DigitalPet> saved = petDAO.findByName("TestPet");
        assertTrue(saved.isPresent());
        assertEquals("TestPet", saved.get().getName());
        assertEquals(150, saved.get().getExperiencePoints());
        assertEquals(PetMood.HAPPY, saved.get().getCurrentMood());
    }
    
    @Test
    @DisplayName("Save existing pet updates record in database")
    void testSaveExistingPet() throws SQLException {
        // Create and save initial pet
        DigitalPet pet = new DigitalPet("UpdatePet");
        pet.setExperiencePoints(100);
        petDAO.save(pet);
        
        // Update pet and save again
        pet.setExperiencePoints(200);
        pet.setCurrentMood(PetMood.CELEBRATING);
        petDAO.save(pet);
        
        // Verify update
        Optional<DigitalPet> updated = petDAO.findByName("UpdatePet");
        assertTrue(updated.isPresent());
        assertEquals(200, updated.get().getExperiencePoints());
        assertEquals(PetMood.CELEBRATING, updated.get().getCurrentMood());
    }
    
    @Test
    @DisplayName("Find by name returns correct pet")
    void testFindByName() throws SQLException {
        // Create multiple pets
        DigitalPet pet1 = new DigitalPet("Pet1");
        DigitalPet pet2 = new DigitalPet("Pet2");
        
        petDAO.save(pet1);
        petDAO.save(pet2);
        
        // Find specific pet
        Optional<DigitalPet> found = petDAO.findByName("Pet1");
        assertTrue(found.isPresent());
        assertEquals("Pet1", found.get().getName());
        
        // Non-existent pet should return empty
        Optional<DigitalPet> notFound = petDAO.findByName("NonExistent");
        assertFalse(notFound.isPresent());
    }
    
    @Test
    @DisplayName("Find most recent returns latest updated pet")
    void testFindMostRecent() throws SQLException {
        // Create pets with different update times
        DigitalPet pet1 = new DigitalPet("OldPet");
        petDAO.save(pet1);
        
        // Wait a bit to ensure different timestamps
        try { Thread.sleep(10); } catch (InterruptedException e) {}
        
        DigitalPet pet2 = new DigitalPet("NewPet");
        petDAO.save(pet2);
        
        Optional<DigitalPet> mostRecent = petDAO.findMostRecent();
        assertTrue(mostRecent.isPresent());
        assertEquals("NewPet", mostRecent.get().getName());
    }
    
    @Test
    @DisplayName("Find all returns all pets in correct order")
    void testFindAll() throws SQLException {
        // Create multiple pets
        DigitalPet pet1 = new DigitalPet("Pet1");
        DigitalPet pet2 = new DigitalPet("Pet2");
        DigitalPet pet3 = new DigitalPet("Pet3");
        
        petDAO.save(pet1);
        petDAO.save(pet2);
        petDAO.save(pet3);
        
        List<DigitalPet> allPets = petDAO.findAll();
        assertEquals(3, allPets.size());
        
        // Should be ordered by most recent first
        assertEquals("Pet3", allPets.get(0).getName());
    }
    
    @Test
    @DisplayName("Delete removes pet from database")
    void testDelete() throws SQLException {
        DigitalPet pet = new DigitalPet("DeleteMe");
        petDAO.save(pet);
        
        // Verify pet exists
        assertTrue(petDAO.exists("DeleteMe"));
        
        // Delete pet
        boolean deleted = petDAO.delete("DeleteMe");
        assertTrue(deleted);
        
        // Verify pet no longer exists
        assertFalse(petDAO.exists("DeleteMe"));
        Optional<DigitalPet> notFound = petDAO.findByName("DeleteMe");
        assertFalse(notFound.isPresent());
    }
    
    @Test
    @DisplayName("Count returns correct number of pets")
    void testCount() throws SQLException {
        assertEquals(0, petDAO.count());
        
        petDAO.save(new DigitalPet("Pet1"));
        assertEquals(1, petDAO.count());
        
        petDAO.save(new DigitalPet("Pet2"));
        assertEquals(2, petDAO.count());
        
        petDAO.delete("Pet1");
        assertEquals(1, petDAO.count());
    }
    
    @Test
    @DisplayName("Exists returns correct boolean values")
    void testExists() throws SQLException {
        assertFalse(petDAO.exists("NonExistent"));
        
        petDAO.save(new DigitalPet("ExistingPet"));
        assertTrue(petDAO.exists("ExistingPet"));
        
        petDAO.delete("ExistingPet");
        assertFalse(petDAO.exists("ExistingPet"));
    }
    
    @Test
    @DisplayName("Update XP and stage works correctly")
    void testUpdateXPAndStage() throws SQLException {
        DigitalPet pet = new DigitalPet("XPPet");
        petDAO.save(pet);
        
        // Update XP and stage
        petDAO.updateXPAndStage("XPPet", 300, EvolutionStage.TEEN);
        
        // Verify update
        Optional<DigitalPet> updated = petDAO.findByName("XPPet");
        assertTrue(updated.isPresent());
        assertEquals(300, updated.get().getExperiencePoints());
        assertEquals(EvolutionStage.TEEN, updated.get().getCurrentStage());
    }
    
    @Test
    @DisplayName("Update mood and energy works correctly")
    void testUpdateMoodAndEnergy() throws SQLException {
        DigitalPet pet = new DigitalPet("MoodPet");
        petDAO.save(pet);
        
        // Update mood and energy
        petDAO.updateMoodAndEnergy("MoodPet", PetMood.SLEEPY, 25);
        
        // Verify update
        Optional<DigitalPet> updated = petDAO.findByName("MoodPet");
        assertTrue(updated.isPresent());
        assertEquals(PetMood.SLEEPY, updated.get().getCurrentMood());
        assertEquals(25, updated.get().getEnergyLevel());
    }
    
    @Test
    @DisplayName("Record interaction updates timestamp")
    void testRecordInteraction() throws SQLException {
        DigitalPet pet = new DigitalPet("InteractionPet");
        LocalDateTime originalTime = pet.getLastInteraction();
        petDAO.save(pet);
        
        // Wait a bit to ensure different timestamp
        try { Thread.sleep(10); } catch (InterruptedException e) {}
        
        // Record interaction
        petDAO.recordInteraction("InteractionPet");
        
        // Verify timestamp was updated
        Optional<DigitalPet> updated = petDAO.findByName("InteractionPet");
        assertTrue(updated.isPresent());
        assertTrue(updated.get().getLastInteraction().isAfter(originalTime));
    }
    
    @Test
    @DisplayName("Pet data persists across database restarts")
    void testDataPersistenceAcrossRestarts() throws SQLException {
        // Create and save pet
        DigitalPet originalPet = new DigitalPet("PersistentPet");
        originalPet.setExperiencePoints(250);
        originalPet.setCurrentStage(EvolutionStage.TEEN);
        originalPet.setCurrentMood(PetMood.HAPPY);
        originalPet.setEnergyLevel(75);
        petDAO.save(originalPet);
        
        // Close and reopen storage (simulating application restart)
        storage.close();
        storage = new LocalDataStorage();
        petDAO = new DigitalPetDAO(storage);
        
        // Verify pet data persisted
        Optional<DigitalPet> persistedPet = petDAO.findByName("PersistentPet");
        assertTrue(persistedPet.isPresent());
        
        DigitalPet pet = persistedPet.get();
        assertEquals("PersistentPet", pet.getName());
        assertEquals(250, pet.getExperiencePoints());
        assertEquals(EvolutionStage.TEEN, pet.getCurrentStage());
        assertEquals(PetMood.HAPPY, pet.getCurrentMood());
        assertEquals(75, pet.getEnergyLevel());
    }
    
    @Test
    @DisplayName("Invalid operations throw appropriate exceptions")
    void testInvalidOperations() {
        // Null pet should throw exception
        assertThrows(IllegalArgumentException.class, () -> petDAO.save(null));
        
        // Update non-existent pet should throw exception
        assertThrows(SQLException.class, () -> 
            petDAO.updateXPAndStage("NonExistent", 100, EvolutionStage.BABY));
        
        // Update with null/empty name should throw exception
        assertThrows(IllegalArgumentException.class, () -> 
            petDAO.updateMoodAndEnergy(null, PetMood.NEUTRAL, 50));
        
        assertThrows(IllegalArgumentException.class, () -> 
            petDAO.updateMoodAndEnergy("", PetMood.NEUTRAL, 50));
    }
    
    @Test
    @DisplayName("Pet name validation works correctly")
    void testPetNameValidation() throws SQLException {
        // Empty name should be replaced with default
        DigitalPet pet1 = new DigitalPet("");
        petDAO.save(pet1);
        
        Optional<DigitalPet> saved1 = petDAO.findByName("My Pet");
        assertTrue(saved1.isPresent());
        
        // Null name should be replaced with default
        DigitalPet pet2 = new DigitalPet(null);
        petDAO.save(pet2);
        
        // Should update the existing "My Pet" record
        Optional<DigitalPet> saved2 = petDAO.findByName("My Pet");
        assertTrue(saved2.isPresent());
    }
}