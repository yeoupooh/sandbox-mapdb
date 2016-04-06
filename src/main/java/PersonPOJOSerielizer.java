import org.ehcache.exceptions.SerializerException;
import org.ehcache.spi.serialization.Serializer;

import java.nio.ByteBuffer;

/**
 * Created by Thomas on 4/6/2016.
 */
public class PersonPOJOSerielizer extends PersonPOJO implements Serializer<PersonPOJO> {
    public ByteBuffer serialize(PersonPOJO object) throws SerializerException {
        ByteBuffer buffer = ByteBuffer.allocate(object.getName().getBytes().length + 4);
        return buffer;
    }

    public PersonPOJO read(ByteBuffer binary) throws ClassNotFoundException, SerializerException {
        PersonPOJO pojo = new PersonPOJO();
        int sizeOfString = binary.getInt();
        byte[] buffer = new byte[sizeOfString];
        pojo.setName(String.valueOf(binary.get(buffer, 0, sizeOfString)));
        pojo.setAge(binary.getInt());
        return pojo;
    }

    public boolean equals(PersonPOJO object, ByteBuffer binary) throws ClassNotFoundException, SerializerException {
        PersonPOJO person = read(binary);
        return object.getName().equals(person.getName()) && object.getAge() == person.getAge();
    }
}
