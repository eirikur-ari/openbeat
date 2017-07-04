# OpenBEAT: Text to Animated Speech Behavior 

OpenBEAT was a BS project for Center for Analysis and Design of Intelligent Agents (CADIA) at Reykjavik University in the spring of 2009.

The project is an open-source application that allows animators to input typed text that they wish to be spoken by an animated human figure. The output of the application is synchronized nonverbal behaviors in a form that can be sent to a number of different animation systems such as [BML Realizer](http://cadia.ru.is/projects/bmlr/).

OpenBEAT is based on [BEAT](http://www.media.mit.edu/gnl/publications/siggraph2001.pdf) but with a different approach in regard of data structure, extensibility as well as it allows developers to use different natural language processing toolkits including English and Icelandic.

The project is developed by: Árni Hermann Reynisson (arnihr@gmail.com), Eiríkur Ari Pétursson (eirikur.ari@hotmail.com) and Guðleifur Kristjánsson (gudleifur@gudleifur.com). [Dr Hannes Högni Vilhjálmsson](http://www.ru.is/faculty/hannes/) is the project supervisor.

## About OpenBEAT
The idea of OpenBEAT as a re-write of BEAT originates both from the fact that BEAT is not an open-source product as well as from the idea of enhancing extensibility of the product. That means that the XML data structure which BEAT relies on had to be replaced with a lighter object model.

### Difference in pipeline structure
The pipeline functionality of BEAT and OpenBEAT relies on the same concept. To illustrate this principle let´s imagine an animator providing a text, to either systems, representing what an animated character is supposed to say.

The text is first processed by a language module that describes the contents, using a range of linguistic markers. It uses several different techniques from the field of Natural Language Processing or NLP.

From these markers a description of supporting behavior is added, based on results from human behavior studies. These results are stored in generators as rules. The text with behavior descriptions is then turned into speech and animation.

### Key focus of OpenBEAT
The key focus of OpenBEAT as an enhancement to BEAT can be divided into the following items:

#### OpenBEAT‘s object model
Because BEAT depends on XML throughout its whole pipeline the internal structure of OpenBEAT had to be somewhat reinvented without breaking the idea of the initial pipeline concept.

##### Object model vs. Xml
Below is a description of the benefits and pitfalls of using Object model instead of XML.

* Java's static typing makes the object model refactor friendly through tool support
* When processing XML in Java, attributes and elements are accessed with java.lang.String which is not refactorable
* Enforces correct model through type system at compile-time, not run-time (strict, not forgiving)
* Unless XML Schema is used, no validation is performed to check if the model is correct. Even when using schemas, the validation is not done until run-time
* Model is viewable and documentable in code
* XML Schema is an interface on how the XML should be formed and could serve similar purpose as we intend our object model. Without a schema, description of the model is going to be scattered or documented outside the code
* XML can have high runtime cost in memory
* If DOM is used, the whole document tree is loaded into memory with all it's parent relations and operations which one can perform on the tree

#### OpenBEAT modularity
OpenBEAT uses both Maven and Guice. Maven is used to split the project into modules and Guice to split the code into modules.

* For behavior that will be subject to changes, interfaces are introduced which adds pluggability and encourages loose coupling
* Code changes do not ripple though the entire system, since each maven module only has access to it's dependencies and it's transitive dependencies (e.g. A → B, B → C ⇒ A → C)

#### Interface for different NLP sources
OpenBEAT relies on an interface called INlpSource which consumes a text input and produces instances of the object model. Since different NLP sources produce different Part-of-speech (POS) tags and constituent tags, each NLP source moves its output to the object model and then normalizes the POS tags and constituent tags. In that way the conversion is placed in the NLP sources while the rest of the pipeline uses the object model and never has to deal with different implementations of NLP sources.

#### Reusable discourse model
A new enhancement to BEAT is a reusable discourse model. OpenBEAT implements an interface for the discourse model, IDiscourseModel that uses recency constraint for co-reference resolution. There is also an interface, IDiscourseTagger, implemented for discourse tagging and a single implementation of it which traverses the instances of the object model which the NLP source created and marks new entities and referring expressions.

#### Plugability for behavior generators
An interface, IBehaviorGenerator, allows plugability of generators. OpenBEAT comes with default implementations which can generate gaze-, headnod- and eyebrows behaviors, for instance. Other implementation can be added and bound to the runtime process with little effort.

#### Output as BML and connection to BML Realizer
The object model can be compiled into BML and sent to BML Realizer. The functionality of the connection from OpenBEAT to BML Realizer can be described in the following way:

* BmlRealizerWriter implements the IOutputWriter interface and receives BML from the process
* BmlRealizerWriter sends the BML to OpenBEATServer integrated into BML Realizer
* OpenBEATServer forwards the BML to an animated character within BML Realizer

#### The Graphical user interface (GUI) of OpenBEAT
Graphical utilization of OpenBEAT is done through OpenBEAT´s GUI. An animator can choose the following items:

* NLP source
* Timing source
* Compiler source
* Writer source
* Behaviors

An input field is provided for an input text from the animator and an output field showing the output of OpenBEAT. There is also an input field for entering the name of a specific animated character in BML Realizer. The animator can either request Dry run or Run. Dry run gives the output without connecting to an animation system but by choosing Run, the output from OpenBEAT is sent to the animation system, such as BML Realizer.
