# Native screenshot evidence

All data is synthetic. `ui-evidence/` is captured by `UiFoundationTest` on the isolated Android 13 Pixel 2 emulator, using native Compose rendering with the real Room-backed app routes for Home/Settings and direct presentation state for theme/font comparisons. Light and dark screenshots use explicit theme inputs. The enlarged-text capture supplies a Compose font scale of 2; it does not certify the device-wide Android accessibility setting or TalkBack.

`home-empty-api33.png` is a full launcher screenshot on the isolated API 33 emulator. The local prototype browser could not be rendered because the browser policy rejected its file URL. These are native captures, not an all-screen pixel comparison with the reference.

See ../NATIVE_VERIFICATION.md for the final capture inventory, test results and limitations. Owner visual review is pending, especially empty/resume/failure states, unavailable actions and the retained manual routine chooser.

`a32/` contains physical Galaxy A32 captures at its original system font scale of 1.1 (the direct theme/font-comparison test still supplies its own explicit scale). `a32-system-200/` contains a separate run with the actual system font scale set to 2.0. Home/Settings route captures in that folder demonstrate system scaling; the explicit theme/font-comparison views still use their test overrides. All four new UI tests passed at the actual system setting. Original font, power and airplane-mode settings were restored. TalkBack and full-app accessibility acceptance remain outstanding.
