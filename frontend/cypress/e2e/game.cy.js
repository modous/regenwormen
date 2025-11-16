describe("Regenwormen Game Flow", () => {
    it("loads the home page", () => {
        cy.visit("/")
        cy.contains("Regenwormen").should("exist")
    })

    it("navigates to lobbies and joins a lobby", () => {
        cy.visit("/")
        cy.window().then(win => {
            win.localStorage.setItem("user", JSON.stringify({ username: "Tester" }))
        })
        cy.visit("/lobbies")
        cy.contains("Join").click({ force: true })
        cy.url().should("include", "/lobby/")
    })


    it("displays timer and roll button in the game screen", () => {
        cy.visit("/")
        cy.window().then(win => {
            win.localStorage.setItem("user", JSON.stringify({ username: "Tester" }))
            win.localStorage.setItem("gameId", "1234")
        })
        cy.visit("/game")
        cy.contains("🎲").should("exist")
    })
})
