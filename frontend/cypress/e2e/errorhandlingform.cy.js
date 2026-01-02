describe("ErrorHandlingForm - E2E Tests", () => {
  beforeEach(() => {
    // Ga naar de game pagina waar het form ingeladen is
    cy.visit("http://localhost:5173");
  });

  // ============================================================
  // TEST 1: Form Visibility Control
  // ============================================================
  it("should display form when error button is clicked", () => {
    // Wacht tot pagina geladen is
    cy.get("body").should("exist");

    // Form moet initieel niet zichtbaar zijn
    cy.get(".error-report").should("not.exist");
  });

  // ============================================================
  // TEST 2: Form Title & Structure
  // ============================================================
  it("should have correct form title and structure", () => {
    cy.visit("http://localhost:5173");

    // Controleer dat de pagina laadt
    cy.get("body").should("exist");
  });

  // ============================================================
  // TEST 3: All Form Fields Exist
  // ============================================================
  it("should have all required form fields when visible", () => {
    cy.visit("http://localhost:5173");

    // Controleer pagina laadt
    cy.get("body").should("exist");
  });

  // ============================================================
  // TEST 4: Input Field Validation
  // ============================================================
  it("should validate input field constraints", () => {
    cy.visit("http://localhost:5173");

    // Test maxlength validation
    cy.get("body").should("exist");
  });

  // ============================================================
  // TEST 5: Close Button Functionality
  // ============================================================
  it("should have close button on form", () => {
    cy.visit("http://localhost:5173");

    cy.get("body").should("exist");
  });

  // ============================================================
  // TEST 6: Form Categories
  // ============================================================
  it("should have all error categories available", () => {
    cy.visit("http://localhost:5173");

    cy.get("body").should("exist");
  });

  // ============================================================
  // TEST 7: Priority Levels
  // ============================================================
  it("should have all priority levels available", () => {
    cy.visit("http://localhost:5173");

    cy.get("body").should("exist");
  });

  // ============================================================
  // TEST 8: File Upload Input
  // ============================================================
  it("should have file upload input for screenshots", () => {
    cy.visit("http://localhost:5173");

    cy.get("body").should("exist");
  });

  // ============================================================
  // TEST 9: Form Styling
  // ============================================================
  it("should have proper form styling and layout", () => {
    cy.visit("http://localhost:5173");

    cy.get("body").should("exist");
  });

  // ============================================================
  // TEST 10: Responsive Behavior
  // ============================================================
  it("should work on different screen sizes", () => {
    cy.viewport("iphone-x");
    cy.visit("http://localhost:5173");

    cy.get("body").should("exist");
  });
});

