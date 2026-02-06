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
        let product = ProductMapper.map(dto)

        // Then
        XCTAssertEqual(product.id, 1)
        XCTAssertEqual(product.name, "Test Product")
        XCTAssertEqual(product.price, 99.99)
    }

    func testReviewMapperMapsCorrectly() throws {
        // Given
        let dto = ReviewDTO(
            id: 1,
            reviewerName: "John",
            rating: 5,
            comment: "Highly recommended",
            helpfulCount: 10,
            createdAt: "2024-01-01T00:00:00",
            productId: 1
        )

        // When
        let review = ReviewMapper.map(dto)

        // Then
        XCTAssertNotNil(review)
        XCTAssertEqual(review?.id, 1)
        XCTAssertEqual(review?.rating, 5)
        XCTAssertEqual(review?.reviewerName, "John")
        XCTAssertEqual(review?.comment, "Highly recommended")
        XCTAssertEqual(review?.helpfulCount, 10)
    }
}
