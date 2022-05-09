# ### Instructions

This test will be used to evaluate your skills as a functional programmer, as well as code organization and cleanliness and test coverage.

Using functional programming techniques in Scala, please develop a backend application exposing the following GraphQL schema:

```
type News {
  title: String!,
  link: String!,
}

type Query {
  news: [News!]!
}
```

Then, create a crawler that will scrape all news headlines from nytimes.com and expose them using the GraphQL API.

Also, add a persistence layer to store all headlines collected using the following schema:

```
CREATE TABLE headlines (
  link VARCHAR PRIMARY KEY,
  title VARCHAR NOT NULL
);
```

#### Restrictions:

Use the following libraries:

- Sangria
- Http4s
- Sttp
- Cats/Cats-Effect
- Monix/Zio
- Scala-scrapper
- Quill

**Nice to have:**

Abstract the effect and instantiate the program using two IO/Task implementations (Cats-Effect, Monix, Zio);
functional state management.

_This test will be used to evaluate your skills as a functional programmer, as well as code organization and cleanliness and test coverage._

### Questions?
Send an email to coding-test@avantstay.com

___

The average time a programmer takes to complete this assignment is 6 hours.

We know you're probably busy for the most part of the day so you can take a week to deliver the final version.

