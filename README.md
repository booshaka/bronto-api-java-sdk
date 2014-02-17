# Bronto Java SDK

The SDK is broken up into two projects:

- The `wsimported` Java models, that live in `com.bronto.api.model`
- The SDK wrapper providing a convenient API for various apps

## Benefits of the SDK

- Asynchronous web service calls
- Retry on common network errors
- Automatic pagination with the `readAll` object operations
- Chainable request building
- Component driven object operations

## Publish Locally

The project is built [SBT](http://www.scala-sbt.org/) because it is a Superior Build Tool.

```
> sbt
> project bronto-api
> publishLocal
```

## Installation

```
<dependency>
  <groupId>com.bronto</groupId>
  <artifactId>bronto-api-sdk</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Example Code

All calls, with the exception of `login` and `readAll`, can be made
asynchronously. This means there's two deritives of an API call:

- The call itself, with expected arguments
- The call with the expected arguments and an `AsyncHandler`, see below

### Login

``` java
import com.bronto.api.*;
import com.bronto.api.model.*;
import static com.bronto.api.model.ObjectBuilder.newObject;

String apiToken = "<You API token>";
BrontoApi client = new BrontoClient(apiToken);

String sessionId = client.login();
```

### Create new Contact

``` java
ContactOperations contactOps = new ContactOperations(client);

ContactObject contact = contactOps.newObject()
  .set("email", "user@example.com")
  .set("status", ContactStatus.ONBOARDING)
  .add("listIds", listId)
  .add("fields", newObject(ContactField.class)
      .set("fieldId", fieldId)
      .set("content", value).get())
  .get();

try {
    WriteResult result = contactOps.add(contact);
} catch (Exception e) {
    // Handle exception
}
```

### Create / Update many Contacts

``` java
contactOps.addOrUpdate(contacts);
```

### Delete a contact

``` java
contactOps.delete(contact);
```

### Read Contacts using Read Request

#### Option #1: Using while loop

``` java
Calendar createdThreshold = Calendar.getInstance();
createdThreshold.add(Calendar.DATE, -7);

final ContactReadRequest readContacts = new ContactReadRequest()
    .withStatus(ContactStatus.TRANSACTIONAL)
    .withCreated(FilterOperator.AFTER, createdThreshold.getTime())
    .withListId(listId);

List<ContactObject> contacts = null;
while(contacts = contactOps.read(readContacts); !contacts.isEmpty()) {
    for (ContactObject contact: contacts) {
        System.out.println(contact.getEmail());
    }
    readContacts.next();
}
```

#### Option #2: Using automatic pager

``` java
for (ContactObject contact : contactOps.readAll(readContacts)) {
    System.out.println(contact.getEmail());
}
```

### Read List by Name

``` java

MailListOperations listOps = new MailListOperations(client);

MailListObject list = listOps.get(new MailListReadRequest().withName("My Example List"));
```

### Clear List(s)

``` java
listOps.clear(list);
```

### Create new Field

``` java
FieldObject field = newObject(FieldObject.class)
    .with("name", "API Field")
    .with("label", "API Field Label")
    .with("visibility", FieldVisibility.PRIVATE)
    .with("type", FieldType.TEXT).get();

client.transport(FieldObject.class).add(field);
```

### Get a ContentTag

``` java
ObjectOperations<ContentTagObject> contentTagOps = client.transport(ContentTagObject.class);

ContentTagObject tag = contentTagOps.get(new ContentTagReadRequest().withId("123"));
```

### Retrieve a Message

``` java
ObjectOperations<MessageObject> messageOps = client.transport(MessageObject.class);

MessageObject message = messageOps.get(new MessageReadRequest().withId("123"));
```

### Create a Delivery

``` java
DeliveryOperations deliveryOps = new DeliveryOperations(client);

DeliveryRecipientObject recipient = new DeliveryRecipientObject();
recipient.setDeliveryType(DeliveryRecipientSelection.SELECTED.getApiValue());
recipient.setType(DeliveryRecipientType.CONTACT.getApiValue());
recipient.setId(contact.getId());

DeliveryObject delivery = deliveryOps.newObject(new Date());
delivery.setType(DeliveryType.TRANSACTIONAL.getApiValue());
delivery.setMessageId(message.getId());
delivery.setFromEmail("user@example.com");
delivery.setFromName("Example Sender");
delivery.getRecipients().add(recipient);

deliveryOps.add(delivery);
```

### Read a Delivery

``` java
deliveryOps.read(new DeliveryReadRequest().setId(delivery.getId())).get();
```

### Read Recipients from a Delivery

``` java
ObjectOperations<DeliveryRecipientStatObject> deliveryStats = client.transport(DeliveryRecipientStatObject.class);
DeliveryRecipientReadRequest readDelivery = new DeliveryRecipientReadRequest().setId(delivery.getId());

for (DeliveryRecipientStatObject stat : deliveryStats.readAll(readDelivery)) {
    // Do something with stat
}
```

## Asynchronous Operations

There's an optional asynchronous version of these specific operations:

- read
- add
- update
- delete

Asynchronous operations are suffixed with `Async`, in the class. The request
building should be transferrable.

## Example Code

### Read Contacts
```
BrontoApiAsync ClientAsync client = new BrontoClientAsync(apiToken, executor);
ContactOperationsAsync contactOps = new ContactOperationsAsync(client);

Calendar createdThreshold = Calendar.getInstance();
createdThreshold.add(Calendar.DATE, -7);

final ContactReadRequest readContacts = new ContactReadRequest()
    .withStatus(ContactStatus.TRANSACTIONAL)
    .withCreated(FilterOperator.AFTER, createdThreshold.getTime())
    .withListId(listId);

contactOps.read(readContacts, new AsyncReadPager<ContactObject>() {
    @Override
    public void readObjects(List<ContactObject> contacts) {
        for (ContactObject contact : contacts) {
            System.out.println("Contact with email: " + contact.getEmail());
        }
    }
    @Override
    public void onError(Exception e) {
        // Handle the exception case
    }
});
```

### Add Contacts
```
contactOps.addOrUpdate(Arrays.asList(contact), new AsyncWriteHandler() {
    @Override
    public void onSuccessItems(List<ResultItem> results) {
        for (ResultItem result : results) {
            System.out.println("Added/Updated contact with id: " + result.getId());
        }
    }
    @Override
    public void onErrorItems(List<ResultItem> results) {
        for (ResultItem result : results) {
            System.err.println(result.getErrorString());
        }
    }
    @Override
    public void onError(Exception e) {
        // Handle the exception case
    }
});
```
