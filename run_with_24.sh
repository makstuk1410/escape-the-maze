#!/bin/bash
cd /home/maks/NetBeansProjects/EscapeTheMaze
JAVA_HOME=/usr/lib/jvm/java-24-openjdk
 OJECT_DIR="/home/maks/NetBeansProjects/EscapeTheMaze"
  DIST_DIR="$PROJECT_DIR/dist"
  JAR_NAME="EscapeTheMaze-1.0-jar-with-dependencies.jar" # або своє
  FINAL_JAR_NAME="EscapeTheMaze.jar"  # як буде називатися у користувача

  # === Перехід в папку проєкту ===
  cd "$PROJECT_DIR" || exit
  
   # === Maven збірка ===
   echo "🔧 Збираю проєкт..."
   JAVA_HOME=/usr/lib/jvm/java-24-openjdk
   /usr/lib/netbeans/java/maven/bin/mvn clean package -DskipTests
  
   # === Створення dist/ і копіювання jar ===
   mkdir -p "$DIST_DIR"
   cp "$PROJECT_DIR/target/$JAR_NAME" "$DIST_DIR/$FINAL_JAR_NAME"
  
   echo "✅ Готово! JAR файл тут: $DIST_DIR/$FINAL_JAR_NAME"
  
   # === [Необов’язково] Запуск JAR (наприклад, для тесту) ===
   echo "🚀 Запускаю гру..."
   java -jar "$DIST_DIR/$FINAL_JAR_NAME"
  
