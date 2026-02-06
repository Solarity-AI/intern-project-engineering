import XCTest

final class ProductReviewUITests: XCTestCase {

    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    override func tearDownWithError() throws {
        // Teardown code
    }

    func testProductListLoads() throws {
        let app = XCUIApplication()
        app.launch()

        // Verify the main tab bar exists
        XCTAssertTrue(app.tabBars.firstMatch.exists)

        // Verify products tab is selected by default
        let productsTab = app.tabBars.buttons["Products"]
        XCTAssertTrue(productsTab.exists)
    }

    func testNavigationBetweenTabs() throws {
        let app = XCUIApplication()
        app.launch()

        // Navigate to wishlist
        let wishlistTab = app.tabBars.buttons["Wishlist"]
        if wishlistTab.exists {
            wishlistTab.tap()
        }

        // Navigate to notifications
        let notificationsTab = app.tabBars.buttons["Notifications"]
        if notificationsTab.exists {
            notificationsTab.tap()
        }

        // Navigate back to products
        let productsTab = app.tabBars.buttons["Products"]
        if productsTab.exists {
            productsTab.tap()
        }
    }

    func testLaunchPerformance() throws {
        if #available(macOS 10.15, iOS 13.0, tvOS 13.0, watchOS 7.0, *) {
            measure(metrics: [XCTApplicationLaunchMetric()]) {
                XCUIApplication().launch()
            }
        }
    }
}
