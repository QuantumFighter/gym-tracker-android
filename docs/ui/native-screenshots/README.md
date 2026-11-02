# Native screenshot evidence

All data is synthetic. `ui-evidence/` is captured by `UiFoundationTest` on the isolated Android 13 Pixel 2 emulator, using native Compose rendering with the real Room-backed app routes for Home/Settings and direct presentation state for theme/font comparisons. Light and dark screenshots use explicit theme inputs. The enlarged-text capture supplies a Compose font scale of 2; it does not certify the device-wide Android accessibility setting or TalkBack.

`home-empty-api33.png` is a full launcher screenshot on the isolated API 33 emulator. The local prototype browser could not be rendered because the browser policy rejected its file URL. These are native captures, not an all-screen pixel comparison with the reference.

See ../NATIVE_VERIFICATION.md for the final capture inventory, test results and limitations. Owner visual review is pending, especially empty/resume/failure states, unavailable actions and the retained manual routine chooser.
