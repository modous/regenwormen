import { describe, it, expect } from "vitest"
import { mount } from "@vue/test-utils"
import { createRouter, createMemoryHistory } from "vue-router"
import GameMain from "@/components/game/GameMain.vue"

const router = createRouter({
    history: createMemoryHistory(),
    routes: [],
})

describe("GameMain.vue - simpele tests", () => {
    it('toont "Joining game..." als game niet klaar is', async () => {
        const wrapper = mount(GameMain, {
            global: { plugins: [router] },
        })
        await router.isReady()
        wrapper.vm.gameReady = false
        expect(wrapper.text()).toContain("Joining game...")
    })

    it("toont foutmelding als errorMsg gezet is", async () => {
        const wrapper = mount(GameMain, {
            global: { plugins: [router] },
        })
        await router.isReady()
        wrapper.vm.gameReady = false
        wrapper.vm.errorMsg = "Test error"
        await wrapper.vm.$nextTick()
        const errEl = wrapper.find(".err")
        expect(errEl.exists()).toBe(true)
        expect(errEl.text()).toBe("Test error")
    })

    it("toggle showRules via help-button", async () => {
        const wrapper = mount(GameMain, {
            global: { plugins: [router] },
        })
        await router.isReady()
        wrapper.vm.gameReady = true
        wrapper.vm.showRules = false
        await wrapper.vm.$nextTick()
        const btn = wrapper.find(".help-button")
        expect(btn.exists()).toBe(true)
        await btn.trigger("click")
        expect(wrapper.vm.showRules).toBe(true)
    })
})
