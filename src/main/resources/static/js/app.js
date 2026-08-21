(() => {
  const statusEl=document.querySelector('#status'),refresh=document.querySelector('#refresh'),start=document.querySelector('#start'),stop=document.querySelector('#stop');
  const loader=document.querySelector('#loader'),message=document.querySelector('#message'),address=document.querySelector('#address'),copy=document.querySelector('#copy');
  let busy=false,current='UNKNOWN';
  const csrf=document.querySelector('meta[name="_csrf"]')?.content,csrfHeader=document.querySelector('meta[name="_csrf_header"]')?.content;
  const setBusy=value=>{busy=value;loader.classList.toggle('hidden',!value);updateButtons()};
  const updateButtons=()=>{refresh.disabled=busy;start.disabled=busy||['RUNNING','STARTING'].includes(current);stop.disabled=busy||['STOPPED','STOPPING'].includes(current)};
  const render=data=>{current=data.state||'ERROR';statusEl.textContent=current;statusEl.className=`badge ${current.toLowerCase()}`;if(data.minecraftAddress)address.textContent=data.minecraftAddress;updateButtons()};
  const notify=(text,error=false)=>{message.textContent=text;message.className=`message${error?' error':''}`;clearTimeout(notify.timer);notify.timer=setTimeout(()=>message.classList.add('hidden'),6000)};
  async function request(path,method='GET',quiet=false){setBusy(true);try{const headers={Accept:'application/json'};if(method!=='GET'&&csrf&&csrfHeader)headers[csrfHeader]=csrf;const response=await fetch(path,{method,headers});let data;try{data=await response.json()}catch{throw new Error('The server returned an unreadable response')};render(data);if(!response.ok||!data.success)throw new Error(data.message||'Request failed');if(!quiet)notify(data.message);return data}catch(error){current='ERROR';render({state:'ERROR'});notify(error.message||'Unable to reach the controller',true)}finally{setBusy(false)}}
  refresh.addEventListener('click',()=>request('api/status','GET',true));
  start.addEventListener('click',()=>request('api/start','POST'));stop.addEventListener('click',()=>request('api/stop','POST'));
  copy.addEventListener('click',async()=>{try{await navigator.clipboard.writeText(address.textContent);notify('Address copied')}catch{notify('Could not copy the address',true)}});
  request('api/status','GET',true);
})();
