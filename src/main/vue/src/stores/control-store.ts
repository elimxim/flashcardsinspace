import { defineStore } from 'pinia'

export interface ControlState {
  sidebarExpanded: boolean
}

export const useControlStore = defineStore('control', {
  state: (): ControlState => {
    return {
      sidebarExpanded: true,
    }
  },
  getters: {
    isSidebarExpanded(): boolean {
      return this.sidebarExpanded
    },
  },
  actions: {
    toggleSidebar() {
      this.sidebarExpanded = !this.sidebarExpanded
    },
  }
})
