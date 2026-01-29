
import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { useTheme } from '../useTheme'
import { nextTick } from 'vue'

describe('useTheme', () => {
    let mockMatchMedia
    let mockLocalStorage

    beforeEach(() => {
        // Mock localStorage
        mockLocalStorage = (() => {
            let store = {}
            return {
                getItem: vi.fn((key) => store[key] || null),
                setItem: vi.fn((key, value) => {
                    store[key] = value.toString()
                }),
                clear: vi.fn(() => {
                    store = {}
                })
            }
        })()

        Object.defineProperty(window, 'localStorage', {
            value: mockLocalStorage,
            writable: true
        })

        // Mock matchMedia
        mockMatchMedia = vi.fn().mockImplementation(query => ({
            matches: false, // Default System is Light
            media: query,
            onchange: null,
            addListener: vi.fn(),
            removeListener: vi.fn(),
            addEventListener: vi.fn(),
            removeEventListener: vi.fn(),
            dispatchEvent: vi.fn(),
        }))

        Object.defineProperty(window, 'matchMedia', {
            writable: true,
            value: mockMatchMedia,
        })

        // Clear DOM state (JSDOM)
        document.documentElement.className = ''
        document.documentElement.removeAttribute('data-theme')
    })

    afterEach(() => {
        vi.restoreAllMocks()
    })

    it('should initialize with default values (auto)', () => {
        const { themeMode } = useTheme()
        expect(themeMode.value).toBe('auto')
    })

    it('should toggle themes correctly (smart skip)', async () => {
        const { toggleTheme, themeMode, setThemeMode } = useTheme()

        // System is Light (mock default: matches=false runs in getSystemIsDark)

        setThemeMode('light')
        expect(themeMode.value).toBe('light')

        // Light -> Dark
        toggleTheme()
        await nextTick()
        expect(themeMode.value).toBe('dark')

        // Dark -> Auto
        // Auto resolves to Light (System Light).
        // Dark (True) !== Auto (False). So it switches to Auto.
        toggleTheme()
        await nextTick()
        expect(themeMode.value).toBe('auto')

        // Auto -> Light
        // Light resolves to Light.
        // Auto (False) === Light (False).
        // Matches! So Smart Skip Logic triggers.
        // Skips Light. Moves to next: Dark.
        toggleTheme()
        await nextTick()
        expect(themeMode.value).toBe('dark')
    })

    it('should update calling applyThemeToDOM', async () => {
        const { setThemeMode } = useTheme()

        setThemeMode('dark')
        await nextTick()

        // Verify via JSDOM properties
        expect(document.documentElement.classList.contains('dark')).toBe(true)
        expect(document.documentElement.getAttribute('data-theme')).toBe('dark')

        setThemeMode('light')
        await nextTick()
        expect(document.documentElement.classList.contains('dark')).toBe(false)
        expect(document.documentElement.getAttribute('data-theme')).toBe('light')
    })

    it('should persist theme to localStorage', async () => {
        const { setThemeMode } = useTheme()

        setThemeMode('dark')
        await nextTick()

        expect(mockLocalStorage.setItem).toHaveBeenCalledWith('theme-mode', 'dark')
    })
})
