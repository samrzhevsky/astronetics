## Инструкция по сборке
- Установить sdk, ndk, Android Studio
- Клонировать этот репозиторий
- В файле `./app/src/main/java/ru/samrzhevsky/astronetics/data/Constants.java` изменить `API_URL` на свой
- `./gradlew build` или открыть и собрать в Android Studio
- APK будет лежать в `./app/build/outputs/apk/debug/`

## Сборка подписанного приложения Build release
Для сборки подписанного APK через Android Studio необходимо: выбрать `Build -> Build Bundle(s) / APK(s) -> Build APK(s)`
