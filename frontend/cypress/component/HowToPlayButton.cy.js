import { mount } from 'cypress/vue'
import RulesOverlay from '../../src/components/Game/HowToPlayButton.vue'

describe('RulesOverlay (HowToPlayButton.vue)', () => {
    it('rendert NIET als visible=false', () => {
        mount(RulesOverlay, { props: { visible: false } })
        cy.get('.rules-overlay').should('not.exist')
    })

    it('rendert WEL als visible=true (met image en close-button)', () => {
        mount(RulesOverlay, { props: { visible: true } })
        cy.get('.rules-overlay').should('exist').and('be.visible')
        cy.get('.rules-content').should('be.visible')
        cy.get('.rules-image').should('be.visible')
        cy.get('.close-button').should('be.visible')
    })

    it('emit "close" bij klik op overlay (dankzij @click.self)', () => {
        const onClose = cy.spy().as('onClose')
        mount(RulesOverlay, { props: { visible: true, onClose } })

        // klik buiten de content (op de grijze laag)
        cy.get('.rules-overlay').click('topLeft')
        cy.get('@onClose').should('have.been.calledOnce')
    })

    it('emit "close" bij klik op de close-button', () => {
        const onClose = cy.spy().as('onClose')
        mount(RulesOverlay, { props: { visible: true, onClose } })

        cy.get('.close-button').click()
        cy.get('@onClose').should('have.been.calledOnce')
    })

    it('GEEN "close" emit bij klik op de content zelf', () => {
        const onClose = cy.spy().as('onClose')
        mount(RulesOverlay, { props: { visible: true, onClose } })

        cy.get('.rules-content').click('center')
        cy.get('@onClose').should('not.have.been.called')
    })
})
