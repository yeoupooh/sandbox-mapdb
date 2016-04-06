import org.jetbrains.annotations.NotNull;
import org.junit.Ignore;
import org.junit.Test;
import org.mapdb.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.ConcurrentMap;

import static org.junit.Assert.assertEquals;

/**
 * Created by Thomas on 4/6/2016.
 */
public class MapDBTest {
    @Test
    public void memoryDB() throws Exception {
        //import org.mapdb.*
        DB db = DBMaker.memoryDB().make();
        ConcurrentMap map = db.hashMap("map").make();
        map.put("something", "here");
        assertEquals("here", map.get("something"));
    }

    @Test
    public void makeFileDB() throws Exception {
        DB db = DBMaker.fileDB("file.db").make();
        ConcurrentMap map = db.hashMap("map").make();
        map.put("something", "here");
        assertEquals("here", map.get("something"));
        db.close();
    }

    @Test
    public void getFromFileDB() throws Exception {
        DB db = DBMaker.fileDB("file.db").make();
        ConcurrentMap map = db.hashMap("map").make();
        assertEquals("here", map.get("something"));
        db.close();
    }

    @Ignore
    @Test
    public void putMassiveDataToFileDB() throws Exception {
        DB db = DBMaker.fileDB("something.db").make();
        ConcurrentMap map = db.hashMap("map").make();
        for (int i = 0; i < 1000000; i++) {
            String key = "something" + i;
            if (i % 100000 == 0) {
                System.out.println(i + ": " + key);
            }
            map.put(key, "here" + i);
        }
        assertEquals("here", map.get("something"));
        db.close();
    }

    @Ignore
    @Test
    public void putMassivePersonDataToFileDB() throws Exception {
        DB db = DBMaker.fileDB("person.db").make();
        ConcurrentMap<String, Person> map = db.hashMap("map", Serializer.STRING, new Person()).make();
        for (int i = 0; i < 5000000; i++) {
            String key = "person" + i;
            Person value = new Person("name" + i, i);
            if (i % 100000 == 0) {
                System.out.println(i + ": " + key + "=" + value);
            }
            map.put(key, value);
        }
        assertEquals(0, new Person().compare(new Person("name100", 100), map.get("person100")));
        db.close();
    }

    @Test
    public void getPersonFromFileDB() throws Exception {
        DB db = DBMaker.fileDB("person.db").make();
        ConcurrentMap<String, Person> map = db.hashMap("map", Serializer.STRING, new Person()).make();
        assertEquals(0, new Person().compare(new Person("name100", 100), map.get("person100")));
        assertEquals(0, new Person().compare(new Person("name1234567", 1234567), map.get("person1234567")));
    }

    class Person implements Serializer<Person>, Serializable {
        String name;
        int age;

        Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        Person() {

        }

        public void serialize(@NotNull DataOutput2 out, @NotNull Person value) throws IOException {
            out.writeUTF(value.name);
            out.writeInt(value.age);
        }

        public Person deserialize(@NotNull DataInput2 input, int available) throws IOException {
            String name = input.readUTF();
            int age = input.readInt();
            return new Person(name, age);
        }

        public int compare(Person o1, Person o2) {
            return o1.name.equals(o2.name) && o1.age == o2.age ? 0 : 1;
        }
    }
}
