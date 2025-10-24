// cypress/component/MusicButton.cy.js
import { mount } from 'cypress/vue'
import MusicButton from '../../src/components/MusicButton.vue'
import tune1 from '@/assets/audio/tune1.mp3'
import tune2 from '@/assets/audio/tune2.mp3'
import tune3 from '@/assets/audio/tune3.mp3'

const playlist = [
    { src: tune1, title: 'Tune 1' },
    { src: tune2, title: 'Tune 2' },
    { src: tune3, title: 'Tune 3' },
]
// helper: fake de readonly getter `paused`
function makeStubs(el) {
    const win = el.ownerDocument.defaultView

    const setPaused = (val) => {
        Object.defineProperty(el, 'paused', {
            configurable: true, // belangrijk zodat we 'm opnieuw kunnen overschrijven
            get: () => val,
        })
    }

    // start als paused = true
    setPaused(true)

    // stub play(): zet paused=false en vuur 'play'
    cy.stub(el, 'play').callsFake(() => {
        setPaused(false)
        el.dispatchEvent(new win.Event('play'))
        return Promise.resolve()
    })

    // stub pause(): zet paused=true en vuur 'pause'
    cy.stub(el, 'pause').callsFake(() => {
        setPaused(true)
        el.dispatchEvent(new win.Event('pause'))
    })
}

describe('MusicButton.vue', () => {
    it('kan play en pause togglen', () => {
        // geen autoplay, zodat we eerst kunnen stubben
        mount(MusicButton, { props: { playlist, autoPlay: false } })

        cy.get('audio').then(($audio) => {
            const el = $audio[0]
            makeStubs(el)
        })

        // klik => play
        cy.get('.badge-button').click()
        cy.get('.music-badge').should('have.class', 'playing')

        // klik => pause
        cy.get('.badge-button').click()
        cy.get('.music-badge').should('not.have.class', 'playing')
    })

    it('kan naar de volgende track gaan', () => {
        mount(MusicButton, { props: { playlist, autoPlay: false } })

        cy.get('audio').then(($audio) => {
            const el = $audio[0]
            makeStubs(el)
        })

        // start en ga naar volgende
        cy.get('.badge-button').click()
        cy.contains('.ctrl', '⏭').click()

        cy.get('audio').then(($a) => {
            const el = $a[0]
            const src = el.getAttribute('src') || el.src
            expect(src).to.include('tune2.mp3')
        })
    })

    it('past volume aan bij slider en bewaart in localStorage', () => {
        // reset voor deterministische test
        cy.window().then((w) => w.localStorage.removeItem('musicBadge.volume'))

        mount(MusicButton, { props: { playlist, autoPlay: false, initialVolume: 0.3 } })

        // 1) Hover zodat het paneel opent (pointer-events aan)
        cy.get('.music-badge').trigger('mouseenter')
        cy.get('.panel')
            .should('have.css', 'opacity', '1')      // paneel zichtbaar
            .and(($el) => {
                // sanity check dat pointer events aan staan
                expect(getComputedStyle($el[0]).pointerEvents).to.not.eq('none')
            })

        // 2) Volume schuiven
        cy.get('input.slider')
            .should('be.visible')
            .invoke('val', 70)
            .trigger('input', { force: true })       // force voor zekerheid in CT

        // 3) Assert audio.volume en localStorage
        cy.get('audio').then(($a) => {
            expect($a[0].volume).to.be.closeTo(0.7, 0.01)
        })
        cy.window().then((w) => {
            expect(w.localStorage.getItem('musicBadge.volume')).to.eq('70')
        })
    })

})