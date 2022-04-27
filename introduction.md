---
layout: default
title: Why JGEA?
nav_order: 2
has_children: false
permalink: why
---

# Why JGEA?
{: .no_toc }

Whenever researchers approach the field of Evolutionary Computation (EC), they soon come across the struggle of choosing a software system to experiment with.
In the absence of ingrained research group practices, newbies might feel lost trying to navigate the maze of available EC frameworks.
Even the Google Search Engine-the wise old man of the game-seems at a loss: when asked about the "best software for evolutionary computation" it prompts a mixture of rather vague and confusing results, spanning across the most exotic programming languages.
Not to mention the 7000 repositories returned by querying Github with the word "evolutionary".
Interestingly, the same does not happen for much-publicized and hyped fields as Machine Learning (ML) or Deep Learning (DL), where there is an immediate convergence on well-established corner stones like Scikit-Learn, Keras, or PyTorch.
This highlights a deep-rooted problem in the EC community: the lack of a go-to software system, addressing both the needs of end users and of developers.

At this point, one wonders why such a problem has been eradicated from other fields, e.g., ML or DL, while remaining pervasive in EC.
Beyond the higher popularity of the aforementioned areas, we speculate there is a deeper reason, that is the complexity and the heterogeneity of the elements involved in EC.
More in detail, an EC framework needs to model a vast gamut of concepts, from genotypes to solutions, passing through genetic operators and fitness measures, which can be of a wide spectrum of types.
On the other hand, implementing a single evolutionary algorithm (EA), without any aim of generalization, is often simple: as a result, several very specialized pieces of software exist that do some variant of EC.

An EC framework should be target-oriented, focusing on the exigencies of the community.
As such, it should be both _solid_ and _extensible_.
In fact, people who resort to EC as a mere tool for solving problems aim at solidity, i.e., they require a simple and usable system, which gives some guarantees, and already has built-in algorithms to be used off-the-rack.
Conversely, EC researchers demand an extensible framework, which allows the implementation of new algorithms or representations leveraging lower level abstractions, and gives the possibility to test them on already written benchmarks, without incurring into divergent change due to the additions.
Both requirements call for a clean and solid modeling of core concepts in EC, based on advanced design and programming techniques, and taking advantage of state-of-the-art design patterns.

We propose the Java General Evolutionary Algorithm (JGEA) framework, which we designed and developed to address the previously mentioned problems.
JGEA is written in the Java programming language (namely Java SE 17), which is naturally-suited for modeling complex and variegate concepts, thanks to its object-orientation and the presence of adequate syntactic constructs, e.g., generics and interfaces.
Moreover, Java is portable and it can also be used in combination with other non-Java tools by virtue of language bindings.
The proposed framework is modular and it displays different levels of abstraction, to accustom the needs of researchers planning to extend it without having to re-write new algorithms from scratch.
To make JGEA accessible to end users, the levels of abstraction go down to the implementation of ready-to-use algorithms.
In addition, we provide JGEA with benchmarks to ease the testing of new algorithms and with some additional features to optimize the execution of experiments and to monitor them.

We have been using and updating JGEA since 2018, and we have employed it for the experimental evaluation in more than \num{25} research papers.
In such articles, ranging from evolutionary robotics to grammatical evolution, we have used JGEA both as end users and as EC researchers, thus we have acted as a test-bed to prove its suitability for both aspects.
