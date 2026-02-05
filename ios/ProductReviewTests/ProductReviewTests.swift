import XCTest
@testable import ProductReview

final class ProductReviewTests: XCTestCase {

    override func setUpWithError() throws {
        // Setup code
    }

    override func tearDownWithError() throws {
        // Teardown code
    }

    func testProductMapperMapsCorrectly() throws {
        // Given
        let dto = ProductDTO(
            id: 1,
            name: "Test Product",
            description: "Test Description",
            categories: ["Electronics"],
            price: 99.99,
            imageUrl: "https://example.com/image.jpg",
            averageRating: 4.5,
            reviewCount: 10,
            ratingBreakdown: nil,
            aiSummary: nil
        )

        // When
        let product = ProductMapper.toDomain(dto)

        // Then
        XCTAssertEqual(product.id, 1)
        XCTAssertEqual(product.name, "Test Product")
        XCTAssertEqual(product.price, 99.99)
    }

    func testReviewMapperMapsCorrectly() throws {
        // Given
        let dto = ReviewDTO(
            id: 1,
            productId: 1,
            rating: 5,
            title: "Great Product",
            comment: "Highly recommended",
            userName: "John",
            createdAt: "2024-01-01T00:00:00",
            helpfulCount: 10
        )

        // When
        let review = ReviewMapper.toDomain(dto)

        // Then
        XCTAssertEqual(review.id, 1)
        XCTAssertEqual(review.rating, 5)
        XCTAssertEqual(review.title, "Great Product")
    }
}
