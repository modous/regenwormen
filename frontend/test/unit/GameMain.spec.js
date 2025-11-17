import { describe, it, expect } from "vitest"
import { mount } from "@vue/test-utils"
import { createRouter, createMemoryHistory } from "vue-router"
import GameMain from "@/components/game/GameMain.vue"

const router = createRouter({
    history: createMemoryHistory(),
    routes: [],
})

describe("GameMain.vue - UI state tests", () => {

    // -----------------------------
    // Shows loading message when game is not ready
    // -----------------------------
    it('laat "Joining game..." zien als game niet klaar is', async () => {
        const wrapper = mount(GameMain, { global: { plugins: [router] } })
        await router.isReady()
        wrapper.vm.gameReady = false
        await wrapper.vm.$nextTick()
        expect(wrapper.text()).toContain("Joining game...")
    })

    // -----------------------------
    // Displays error message when errorMsg is set
    // -----------------------------
    it("laat foutmelding zien als errorMsg is gezet", async () => {
        const wrapper = mount(GameMain, { global: { plugins: [router] } })
        await router.isReady()
        wrapper.vm.errorMsg = "Test error"
        await wrapper.vm.$nextTick()
        const errEl = wrapper.find(".err")
        expect(errEl.exists()).toBe(true)
        expect(errEl.text()).toBe("Test error")
    })

    // -----------------------------
    // Enables rules modal when help button is clicked
    // -----------------------------
    it("zet showRules op true als help-button wordt geklikt", async () => {
        const wrapper = mount(GameMain, { global: { plugins: [router] } })
        await router.isReady()

        wrapper.vm.gameReady = true
        wrapper.vm.showRules = false
        await wrapper.vm.$nextTick()

        const btn = wrapper.find(".help-button")
        expect(btn.exists()).toBe(true)

        await btn.trigger("click")
        await wrapper.vm.$nextTick()

        expect(wrapper.vm.showRules).toBe(true)
    })

    // -----------------------------
    // Hides loading message when gameReady is true
    // -----------------------------
    it("laat geen 'Joining game...' zien als gameReady true is", async () => {
        const wrapper = mount(GameMain, { global: { plugins: [router] } })
        await router.isReady()
        wrapper.vm.gameReady = true
        await wrapper.vm.$nextTick()
        expect(wrapper.text()).not.toContain("Joining game...")
    })

    // -----------------------------
    // Does not render an error element when errorMsg is empty
    // -----------------------------
    it("handelt lege errorMsg correct af", async () => {
        const wrapper = mount(GameMain, { global: { plugins: [router] } })
        await router.isReady()
        wrapper.vm.errorMsg = ""
        await wrapper.vm.$nextTick()
        expect(wrapper.find(".err").exists()).toBe(false)
    })
})
