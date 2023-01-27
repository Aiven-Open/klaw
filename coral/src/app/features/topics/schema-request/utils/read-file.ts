async function readFile(file: File | undefined): Promise<string> {
  return new Promise<string>((resolve) => {
    if (file) {
      const reader = new FileReader();
      reader.onload = () => {
        const text = reader.result?.toString();

        if (text) {
          resolve(text);
        } else {
          // @todo reject an empty file
          // and add error handling in areas
          // that use this
          // reject("The file is empty");
          resolve("");
        }
      };
      reader.readAsText(file);
    } else {
      resolve("");
    }
  });
}

export { readFile };
