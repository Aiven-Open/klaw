async function readFile(file: File | undefined): Promise<string> {
  return new Promise<string>((resolve, reject) => {
    if (file) {
      const reader = new FileReader();
      reader.onload = () => {
        const text = reader.result?.toString();
        if (text) {
          resolve(text);
        } else {
          reject("The file is not valid");
        }
      };
      reader.readAsText(file);
    } else {
      resolve("");
    }
  });
}

export { readFile };
