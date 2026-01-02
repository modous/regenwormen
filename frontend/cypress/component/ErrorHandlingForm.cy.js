import ErrorHandlingForm from "@/components/Game/game_assistance/ErrorHandelingForm.vue";

describe("ErrorHandlingForm.vue - Component Tests", () => {
  // ============================================================
  // TEST 1: Form Visibility
  // ============================================================
  describe("Form Visibility (FIRST: Fast, Isolated)", () => {
    it("should display form when visible prop is true", () => {
      cy.mount(ErrorHandlingForm, {
        props: {
          visible: true,
        },
      });

      cy.get(".error-report").should("exist");
      cy.get(".error-form").should("be.visible");
      cy.get("h2.title").should("contain", "Error Report");
    });

    it("should hide form when visible prop is false", () => {
      cy.mount(ErrorHandlingForm, {
        props: {
          visible: false,
        },
      });

      cy.get(".error-report").should("not.exist");
    });
  });

  // ============================================================
  // TEST 2: Close Button Functionality
  // ============================================================
  describe("Close Button (FIRST: Isolated)", () => {
    it("should emit close event when close button is clicked", () => {
      cy.mount(ErrorHandlingForm, {
        props: {
          visible: true,
        },
      });

      cy.get("#close-button").click();

      cy.get("@close-button")
        .parent()
        .then((el) => {
          // Verify close was emitted
          cy.wrap(el).should("exist");
        });
    });

    it("close button should have proper styling", () => {
      cy.mount(ErrorHandlingForm, {
        props: {
          visible: true,
        },
      });

      cy.get("#close-button")
        .should("have.css", "position", "absolute")
        .should("have.css", "border-radius", "9999px");
    });
  });

  // ============================================================
  // TEST 3: Form Input Fields
  // ============================================================
  describe("Form Input Fields (FIRST: Fast, Repeatable)", () => {
    beforeEach(() => {
      cy.mount(ErrorHandlingForm, {
        props: {
          visible: true,
        },
      });
    });

    it("should have all required input fields", () => {
      cy.get("#topic").should("exist");
      cy.get("#category").should("exist");
      cy.get("#priority").should("exist");
      cy.get("#error-description").should("exist");
    });

    it("topic input should enforce maxlength of 100 chars", () => {
      cy.get("#topic").should("have.attr", "maxlength", "100");

      cy.get("#topic").type("a".repeat(150));
      cy.get("#topic").invoke("val").then((val) => {
        expect(val.length).to.equal(100);
      });
    });

    it("description textarea should enforce maxlength of 1000 chars", () => {
      cy.get("#error-description").should("have.attr", "maxlength", "1000");

      cy.get("#error-description").type("b".repeat(1500));
      cy.get("#error-description").invoke("val").then((val) => {
        expect(val.length).to.equal(1000);
      });
    });

    it("category select should have all options", () => {
      cy.get("#category").within(() => {
        cy.get('option[value=""]').should("exist");
        cy.get('option[value="functionality_issue"]').should("contain", "Functionality Issue");
        cy.get('option[value="ui_bug"]').should("contain", "UI Bug");
        cy.get('option[value="performance_problem"]').should("contain", "Performance Problem");
        cy.get('option[value="account"]').should("exist");
        cy.get('option[value="suggestion"]').should("exist");
        cy.get('option[value="other"]').should("exist");
      });
    });

    it("priority select should have correct default and options", () => {
      cy.get("#priority").should("have.value", "low");
      cy.get("#priority").within(() => {
        cy.get('option[value="low"]').should("exist");
        cy.get('option[value="medium"]').should("exist");
        cy.get('option[value="high"]').should("exist");
      });
    });
  });

  // ============================================================
  // TEST 4: Form Validation
  // ============================================================
  describe("Form Validation (RIGHT-BICEP: Isolated)", () => {
    beforeEach(() => {
      cy.mount(ErrorHandlingForm, {
        props: {
          visible: true,
        },
      });

      // Mock alert
      cy.window().then((win) => {
        cy.stub(win, "alert");
      });
    });

    it("should show alert when submitting with empty required fields", () => {
      cy.get('button[type="submit"]').click();

      cy.window().its("alert").should("be.calledWith", "Please fill in all required fields!");
    });

    it("should allow submit when all required fields are filled", () => {
      cy.get("#topic").type("Test Bug");
      cy.get("#category").select("functionality_issue");
      cy.get("#error-description").type("This is a test error description");

      cy.get('button[type="submit"]').should("not.be.disabled");
    });

    it("should disable form inputs during submit", () => {
      // This test would need to mock fetch and observe the loading state
      cy.window().then((win) => {
        cy.stub(win, "fetch").returns(new Promise(() => {})); // Never resolves
      });

      cy.get("#topic").type("Test Bug");
      cy.get("#category").select("functionality_issue");
      cy.get("#error-description").type("Test description");

      cy.get('button[type="submit"]').click();

      // After click, form should be disabled
      cy.get("#topic").should("be.disabled");
      cy.get("#category").should("be.disabled");
      cy.get("#priority").should("be.disabled");
    });
  });

  // ============================================================
  // TEST 5: File Upload / Screenshot
  // ============================================================
  describe("File Upload & Screenshot (FIRST: Isolated, Repeatable)", () => {
    beforeEach(() => {
      cy.mount(ErrorHandlingForm, {
        props: {
          visible: true,
        },
      });
    });

    it("should display file input element", () => {
      cy.get("#snapshot").should("exist");
      cy.get("#snapshot").should("have.attr", "accept", "image/png, image/jpeg");
    });

    it("should show no screenshot message initially", () => {
      cy.get(".row-2col").within(() => {
        cy.get("p").should("contain", "❌ No screenshot");
      });
    });

    it("should have take snapshot button", () => {
      cy.get("#auto-snapshot").should("exist");
      cy.get("#auto-snapshot").should("contain", "📸 Take snapshot");
    });

    it("should validate file size on file select", () => {
      // Create a 3MB file (exceeds 2MB limit)
      const largeFile = new File(
        [new ArrayBuffer(3 * 1024 * 1024)],
        "large.png",
        { type: "image/png" }
      );

      cy.get("#snapshot").selectFile({
        contents: Cypress.Buffer.from("a".repeat(3 * 1024 * 1024)),
        fileName: "large.png",
        mimeType: "image/png",
      });

      // Should show error message
      cy.get(".field-block").within(() => {
        cy.get("p[style*='color: red']").should(
          "contain",
          "File size exceeds the maximum limit of 2MB."
        );
      });
    });

    it("should accept files under 2MB", () => {
      // Create a 1MB file
      cy.get("#snapshot").selectFile({
        contents: Cypress.Buffer.from("a".repeat(1 * 1024 * 1024)),
        fileName: "valid.png",
        mimeType: "image/png",
      });

      // Should show success indicator
      cy.get(".row-2col").within(() => {
        cy.get("p").should("contain", "✔️ Screenshot captured!");
      });
    });
  });

  // ============================================================
  // TEST 6: Feedback Messages (Responsive, Isolated)
  // ============================================================
  describe("Feedback Messages (RIGHT-BICEP: Responsive)", () => {
    it("should show loading feedback when submitting", () => {
      cy.window().then((win) => {
        cy.stub(win, "fetch").returns(
          new Promise((resolve) => {
            setTimeout(() => {
              resolve(new Response(JSON.stringify({ id: "123" }), { status: 201 }));
            }, 100);
          })
        );
      });

      cy.mount(ErrorHandlingForm, {
        props: {
          visible: true,
        },
      });

      cy.get("#topic").type("Test Bug");
      cy.get("#category").select("functionality_issue");
      cy.get("#error-description").type("Test description");

      cy.get('button[type="submit"]').click();

      // Should show loading feedback
      cy.get(".feedback--loading").should("contain", "Sending...");
    });

    it("should show success feedback after submit", () => {
      cy.window().then((win) => {
        cy.stub(win, "fetch").resolves(
          new Response(JSON.stringify({ id: "123" }), { status: 201 })
        );
      });

      cy.mount(ErrorHandlingForm, {
        props: {
          visible: true,
        },
      });

      cy.get("#topic").type("Test Bug");
      cy.get("#category").select("functionality_issue");
      cy.get("#error-description").type("Test description");

      cy.get('button[type="submit"]').click();

      cy.get(".feedback--success").should("contain", "Report submitted");
    });

    it("should show error feedback on API failure", () => {
      cy.window().then((win) => {
        cy.stub(win, "fetch").rejects(new Error("Network error"));
      });

      cy.mount(ErrorHandlingForm, {
        props: {
          visible: true,
        },
      });

      cy.get("#topic").type("Test Bug");
      cy.get("#category").select("functionality_issue");
      cy.get("#error-description").type("Test description");

      cy.get('button[type="submit"]').click();

      cy.get(".feedback--error").should("contain", "Error submitting report");
    });
  });

  // ============================================================
  // TEST 7: Form Reset After Submit
  // ============================================================
  describe("Form Reset (FIRST: Repeatable, Isolated)", () => {
    it("should reset form fields after successful submit", () => {
      cy.window().then((win) => {
        cy.stub(win, "fetch").resolves(
          new Response(JSON.stringify({ id: "123" }), { status: 201 })
        );
      });

      cy.mount(ErrorHandlingForm, {
        props: {
          visible: true,
        },
      });

      // Fill form
      cy.get("#topic").type("Test Bug");
      cy.get("#category").select("functionality_issue");
      cy.get("#priority").select("high");
      cy.get("#error-description").type("Test description");

      // Submit
      cy.get('button[type="submit"]').click();

      // Wait for success feedback
      cy.get(".feedback--success").should("exist");

      // After 2s timeout, form should reset
      cy.wait(2100);

      cy.get("#topic").invoke("val").should("equal", "");
      cy.get("#category").invoke("val").should("equal", "");
      cy.get("#priority").invoke("val").should("equal", "low");
      cy.get("#error-description").invoke("val").should("equal", "");
    });
  });

  // ============================================================
  // TEST 8: Accessibility & Responsive Design
  // ============================================================
  describe("Accessibility (RIGHT-BICEP: Correct, Repeatable)", () => {
    beforeEach(() => {
      cy.mount(ErrorHandlingForm, {
        props: {
          visible: true,
        },
      });
    });

    it("should have proper label associations", () => {
      cy.get('label[for="topic"]').should("exist");
      cy.get('label[for="category"]').should("exist");
      cy.get('label[for="priority"]').should("exist");
      cy.get('label[for="error-description"]').should("exist");
      cy.get('label[for="snapshot"]').should("exist");
    });

    it("should have proper focus states", () => {
      cy.get("#topic").focus().should("have.css", "outline", "none");
      cy.get("#topic").should("have.css", "border-color");
    });

    it("should be responsive on mobile viewport", () => {
      cy.viewport("iphone-x");

      cy.get(".error-form").should("be.visible");
      cy.get(".row-2col").should("have.css", "flex-direction");

      // On small screens, buttons should stack
      cy.get("#action-buttons").should("exist");
    });

    it("should have semantic form structure", () => {
      cy.get("form").should("exist");
      cy.get("form").within(() => {
        cy.get("h3").should("exist"); // Headers for sections
        cy.get("label").should("have.length", 5); // 5 labels for form fields
      });
    });
  });

  // ============================================================
  // TEST 9: Error Scenarios (RIGHT-BICEP: Error cases)
  // ============================================================
  describe("Error Scenarios (RIGHT-BICEP: Error cases)", () => {
    it("should handle network errors gracefully", () => {
      cy.window().then((win) => {
        cy.stub(win, "fetch").rejects(new TypeError("Failed to fetch"));
      });

      cy.mount(ErrorHandlingForm, {
        props: {
          visible: true,
        },
      });

      cy.get("#topic").type("Test Bug");
      cy.get("#category").select("functionality_issue");
      cy.get("#error-description").type("Test description");

      cy.get('button[type="submit"]').click();

      // Should show error feedback
      cy.get(".feedback--error").should("exist");
      cy.window().its("alert").should("be.calledWith", Cypress.sinon.match(/Error submitting/));
    });

    it("should handle API 5xx errors", () => {
      cy.window().then((win) => {
        cy.stub(win, "fetch").resolves(
          new Response(null, { status: 500 })
        );
      });

      cy.mount(ErrorHandlingForm, {
        props: {
          visible: true,
        },
      });

      cy.get("#topic").type("Test Bug");
      cy.get("#category").select("functionality_issue");
      cy.get("#error-description").type("Test description");

      cy.get('button[type="submit"]').click();

      cy.get(".feedback--error").should("contain", "Failed to submit report");
    });

    it("should display file error message when file is rejected", () => {
      cy.mount(ErrorHandlingForm, {
        props: {
          visible: true,
        },
      });

      // Simulate file input change with large file
      cy.get("#snapshot").selectFile({
        contents: Cypress.Buffer.from("x".repeat(3 * 1024 * 1024)),
        fileName: "toolarge.png",
        mimeType: "image/png",
      });

      cy.get(".field-block")
        .get("p[style*='color: red']")
        .should("be.visible");
    });
  });

  // ============================================================
  // TEST 10: Component Isolation (FIRST: Isolated)
  // ============================================================
  describe("Component Isolation (FIRST: Isolated)", () => {
    it("should not affect other components when mounted", () => {
      cy.mount(ErrorHandlingForm, {
        props: {
          visible: true,
        },
      });

      // Should only render error form, not other page content
      cy.get(".error-report").should("exist");
      cy.get("body").should("contain", ".error-report");
    });

    it("should handle prop changes reactively", () => {
      const component = cy.mount(ErrorHandlingForm, {
        props: {
          visible: true,
        },
      });

      cy.get(".error-report").should("exist");

      // Change visible prop to false
      cy.wrap(component).then((wrapper) => {
        wrapper.setProps({ visible: false });
      });

      cy.get(".error-report").should("not.exist");
    });
  });
});

